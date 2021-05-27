package iws.backend

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        post "/user"(controller: 'conversationEngine', action: 'createUser')
        post "/session"(controller: 'conversationEngine', action: 'createSession')
        delete "/session"(controller: 'conversationEngine', action: 'deleteSession')
        get "/step"(controller: 'conversationEngine', action: 'getStep')
        put "/step"(controller: 'conversationEngine', action: 'putStep')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
