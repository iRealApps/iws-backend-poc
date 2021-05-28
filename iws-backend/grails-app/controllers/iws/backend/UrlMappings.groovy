package iws.backend

class UrlMappings {

  static mappings = {
    delete "/$controller/$id(.$format)?"(action: "delete")
    get "/$controller(.$format)?"(action: "index")
    get "/$controller/$id(.$format)?"(action: "show")
    post "/$controller(.$format)?"(action: "save")
    put "/$controller/$id(.$format)?"(action: "update")
    patch "/$controller/$id(.$format)?"(action: "patch")

    post "/api/user"(controller: 'conversationEngine', action: 'createUser')
    post "/api/session"(controller: 'conversationEngine', action: 'createSession')
    delete "/api/session"(controller: 'conversationEngine', action: 'deleteSession')
    get "/api/user/step"(controller: 'conversationEngine', action: 'getUserStep')
    put "/api/user/step"(controller: 'conversationEngine', action: 'putUserStep')

    "/"(controller: 'application', action: 'index')
    "500"(view: '/error')
    "404"(view: '/notFound')
  }
}
