package rest.addressbook

import org.glassfish.jersey.server.ResourceConfig
import org.springframework.stereotype.Component

@Component
class JerseyConfig : ResourceConfig() {
  init {
    this.register(AddressBookController::class.java)
  }
}

