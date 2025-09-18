package example.com.products.infra.route

import io.ktor.resources.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing

@Resource("/api/products")
class Products(val term: String? = "name") {
    @Resource("{id}")
    class Id(val parent: Products = Products(), val id: String) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

fun Application.productRouting() {
    install(Resources)
    routing {
            get<Products> { product ->
                call.respondText("All products matching ${product.term}")
            }
            post<Products>{
                call.respondText("Create product")
            }
            get<Products.Id> { product ->
                call.respondText("Product #${product.id}")
            }
            get<Products.Id.Edit> { product ->
                call.respondText("Edit #${product.parent.id}")
            }

    }
}