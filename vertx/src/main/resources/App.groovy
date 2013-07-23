// Our application config

def persistorConf = [
        address: 'hello.persistor',
        db_name: 'hello_world',
        host: 'localhost'
]

def permitted =
    [
            [
                    'address' : 'hello.persistor'
            ]
    ]

container.with {

    // Deploy the busmods
    deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', persistorConf, 8)

    // Start the web server

    deployVerticle('vertx2.WebServer', ['permitted': permitted], 8)
}
