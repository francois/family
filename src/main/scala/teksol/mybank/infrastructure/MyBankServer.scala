package teksol.mybank.infrastructure

import java.util.concurrent.TimeUnit

import org.eclipse.jetty.server.Server
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import teksol.Config

class MyBankServer(address: String, port: Int) {
    Assert.notNull(address, "address")
    Assert.isTrue(0 <= port && port <= 65535, "port must be (0..65535), found " + port)

    private[this] val log = LoggerFactory.getLogger(this.getClass)

    /**
      * Boots the Jetty server instance and waits for connection requests.
      *
      * NOTE: This method never returns.
      */
    def join(config: Config): Unit = {
        val server = new Server(port)
        server.setHandler(new MyBankHandler(config))
        new Thread(() => {
            server.start()
            server.join()
        }, "MyBank-HTTP-Server").start()

        TimeUnit.SECONDS.sleep(2)
        log.info("Press any key to stop Jetty server")
        System.in.read()
        server.stop()
    }
}
