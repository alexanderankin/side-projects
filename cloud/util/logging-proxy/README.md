# logging proxy

the way these projects work:

* `logged-application` is an implementation of an echo server
* all the others are implementations of reverse proxy which knows how to log
  * based on [`openresty`](./openresty) with lua
    * incomplete, unlikely to be resolved
    * issues with lua api completeness/correctness
  * based on [`spring-boot`](./spring-boot) web(mvc)
    * incomplete, unlikely to be resolved
    * issues stemming from high abstraction
    * have to track down response handling, timeouts
    * would be more work to implement my own spring-y http client
  * based on [`spring-boot-netty`](./spring-boot-netty) (aka `spring-boot-webflux`)
    * working as need based on manual testing
    * seems to be the right tool for the job
    * implemented last as async api is discouraging from a maintenance standpoint
