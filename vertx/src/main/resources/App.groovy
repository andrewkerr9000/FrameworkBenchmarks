// Our application config

def persistorConf = [
        address: 'hello.persistor',
        db_name: 'hello_world',
        host: 'localhost'
]

container.with {

    // Deploy the busmods
    deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', persistorConf, 8) { persistorResult ->
        if (persistorResult.succeeded) {
            deployVerticle('hellovertx.WebServer', [:], 8){ webServerResult ->
                if (webServerResult.succeeded) {
                    println "The WebServer has been deployed, deployment ID is ${webServerResult.result}"
                } else {
                    webServerResult.cause.printStackTrace()
                }
            }
        } else {
            persistorResult.cause.printStackTrace()
        }
    }

    // Start the web server


}
