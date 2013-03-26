package lila.user

import scala.concurrent.duration._

import spray.caching.{ LruCache, Cache }

import play.api.libs.concurrent.Execution.Implicits._

final class Cached(userRepo: UserRepo, ttl: Duration) {

  def username(id: String): Fu[Option[String]] =
    usernameCache.fromFuture(id.toLowerCase)(userRepo usernameById id)

  def usernameOrAnonymous(id: String): Fu[String] = 
    username(id) map (_ | Users.anonymous)

  def usernameOrAnonymous(id: Option[String]): Fu[String] = 
    id.fold(fuccess(Users.anonymous))(usernameOrAnonymous)

  def countEnabled: Fu[Int] = countEnabledCache.fromFuture(true)(userRepo.countEnabled)

  // id => username
  private val usernameCache: Cache[Option[String]] = LruCache(maxCapacity = 99999)

  private val countEnabledCache: Cache[Int] = LruCache(timeToLive = ttl)
}
