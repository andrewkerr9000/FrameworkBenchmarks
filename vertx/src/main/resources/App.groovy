// Our application config

def persistorConf = [
        address: 'hello.persistor',
        db_name: 'hello_world',
        host: 'localhost'
]

container.with {

    //modules and verticles are deployed asynchronously, and we want to know then they're all ready.
    deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', persistorConf, 8) { persistorResult ->
        if (persistorResult.succeeded) {
            deployVerticle('hellovertx.WebServer', [:], 8){ webServerResult ->
                if (webServerResult.succeeded) {
                    // This magic string is looked for by the startup script to indicate that WebServer is ready to roll
                    println "The WebServer has been deployed, deployment ID is ${webServerResult.result}"
                } else {
                    webServerResult.cause.printStackTrace()
                }
            }
        } else {
            persistorResult.cause.printStackTrace()
        }
    }
}
