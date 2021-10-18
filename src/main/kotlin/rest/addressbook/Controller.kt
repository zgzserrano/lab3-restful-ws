package rest.addressbook

import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

/**
 * The (shared) address book object.
 */
var addressBook: AddressBook = AddressBook()

/**
 * A service that manipulates contacts in an address book.
 */
@Path("/contacts")
class AddressBookController {

    /**
     * A GET /contacts request should return the address book in JSON.
     *
     * @return a JSON representation of the address book.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getPersons(): Response = Response.ok().entity(addressBook.personList).build()

    /**
     * A POST /contacts request should add a new entry to the address book.
     *
     * @param info   the URI information of the request
     * @param person the posted entity
     * @return a JSON representation of the new entry that should be available at
     * /contacts/person/{id}.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun addPerson(@Context info: UriInfo, person: Person): Response = with(addressBook) {
        val newId = nextId()
        person.copy(
            id = newId,
            href = info.absolutePathBuilder.path("person/{id}").build(newId)
        ).let { newPerson ->
            personList.add(newPerson)
            Response.created(newPerson.href).entity(newPerson).build()
        }
    }

    /**
     * A GET /contacts/person/{id} request should return a entry from the address book
     *
     * @param id the unique identifier of a person
     * @return a JSON representation of the new entry or 404
     */
    @GET
    @Path("/person/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPerson(@PathParam("id") id: Int): Response = addressBook
        .personList
        .find { person -> person.id == id }
        ?.let { person -> Response.ok(person).build() }
        ?: Response.status(Response.Status.NOT_FOUND).build()

    /**
     * A PUT /contacts/person/{id} should update an entry if exists
     *
     * @param info   the URI information of the request
     * @param update the posted entity
     * @param id     the unique identifier of a person
     * @return  204 if the request is successful, or 404 if the id is not a key
     */
    @PUT
    @Path("/person/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updatePerson(
        @Context info: UriInfo,
        @PathParam("id") id: Int, update: Person
    ): Response = with(addressBook) {
        when {
            personList.removeIf { person -> person.id == id } -> {
                personList.add(update.copy(id = id, href = info.absolutePath))
                Response.noContent().build()
            }
            else -> Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    /**
     * A DELETE /contacts/person/{id} should delete a entry if exists
     *
     * @param id the unique identifier of a person
     * @return 204 if the request is successful or if it was deleted, or 404 if the id is not a key
     */
    @DELETE
    @Path("/person/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updatePerson(@PathParam("id") id: Int): Response = with(addressBook) {
        when {
            personList.removeIf { person -> person.id == id } -> Response.noContent().build()
            id < nextId -> Response.noContent().build()
            else -> Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}