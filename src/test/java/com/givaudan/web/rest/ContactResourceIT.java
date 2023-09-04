package com.givaudan.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.givaudan.IntegrationTest;
import com.givaudan.domain.Contact;
import com.givaudan.repository.ContactRepository;
import com.givaudan.service.dto.ContactDTO;
import com.givaudan.service.mapper.ContactMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ContactResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ContactResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_PRENOM = "AAAAAAAAAA";
    private static final String UPDATED_PRENOM = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;
    private static final Integer SMALLER_AGE = 1 - 1;

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final Integer DEFAULT_CODEPOSTAL = 1;
    private static final Integer UPDATED_CODEPOSTAL = 2;
    private static final Integer SMALLER_CODEPOSTAL = 1 - 1;

    private static final String ENTITY_API_URL = "/api/contacts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactMapper contactMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restContactMockMvc;

    private Contact contact;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contact createEntity(EntityManager em) {
        Contact contact = new Contact()
            .nom(DEFAULT_NOM)
            .prenom(DEFAULT_PRENOM)
            .age(DEFAULT_AGE)
            .address(DEFAULT_ADDRESS)
            .codepostal(DEFAULT_CODEPOSTAL);
        return contact;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contact createUpdatedEntity(EntityManager em) {
        Contact contact = new Contact()
            .nom(UPDATED_NOM)
            .prenom(UPDATED_PRENOM)
            .age(UPDATED_AGE)
            .address(UPDATED_ADDRESS)
            .codepostal(UPDATED_CODEPOSTAL);
        return contact;
    }

    @BeforeEach
    public void initTest() {
        contact = createEntity(em);
    }

    @Test
    @Transactional
    void createContact() throws Exception {
        int databaseSizeBeforeCreate = contactRepository.findAll().size();
        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);
        restContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(contactDTO)))
            .andExpect(status().isCreated());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeCreate + 1);
        Contact testContact = contactList.get(contactList.size() - 1);
        assertThat(testContact.getNom()).isEqualTo(DEFAULT_NOM);
        assertThat(testContact.getPrenom()).isEqualTo(DEFAULT_PRENOM);
        assertThat(testContact.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testContact.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testContact.getCodepostal()).isEqualTo(DEFAULT_CODEPOSTAL);
    }

    @Test
    @Transactional
    void createContactWithExistingId() throws Exception {
        // Create the Contact with an existing ID
        contact.setId(1L);
        ContactDTO contactDTO = contactMapper.toDto(contact);

        int databaseSizeBeforeCreate = contactRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(contactDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllContacts() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList
        restContactMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contact.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].prenom").value(hasItem(DEFAULT_PRENOM)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].codepostal").value(hasItem(DEFAULT_CODEPOSTAL)));
    }

    @Test
    @Transactional
    void getContact() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get the contact
        restContactMockMvc
            .perform(get(ENTITY_API_URL_ID, contact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(contact.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.prenom").value(DEFAULT_PRENOM))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.codepostal").value(DEFAULT_CODEPOSTAL));
    }

    @Test
    @Transactional
    void getContactsByIdFiltering() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        Long id = contact.getId();

        defaultContactShouldBeFound("id.equals=" + id);
        defaultContactShouldNotBeFound("id.notEquals=" + id);

        defaultContactShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultContactShouldNotBeFound("id.greaterThan=" + id);

        defaultContactShouldBeFound("id.lessThanOrEqual=" + id);
        defaultContactShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllContactsByNomIsEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where nom equals to DEFAULT_NOM
        defaultContactShouldBeFound("nom.equals=" + DEFAULT_NOM);

        // Get all the contactList where nom equals to UPDATED_NOM
        defaultContactShouldNotBeFound("nom.equals=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllContactsByNomIsInShouldWork() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where nom in DEFAULT_NOM or UPDATED_NOM
        defaultContactShouldBeFound("nom.in=" + DEFAULT_NOM + "," + UPDATED_NOM);

        // Get all the contactList where nom equals to UPDATED_NOM
        defaultContactShouldNotBeFound("nom.in=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllContactsByNomIsNullOrNotNull() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where nom is not null
        defaultContactShouldBeFound("nom.specified=true");

        // Get all the contactList where nom is null
        defaultContactShouldNotBeFound("nom.specified=false");
    }

    @Test
    @Transactional
    void getAllContactsByNomContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where nom contains DEFAULT_NOM
        defaultContactShouldBeFound("nom.contains=" + DEFAULT_NOM);

        // Get all the contactList where nom contains UPDATED_NOM
        defaultContactShouldNotBeFound("nom.contains=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllContactsByNomNotContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where nom does not contain DEFAULT_NOM
        defaultContactShouldNotBeFound("nom.doesNotContain=" + DEFAULT_NOM);

        // Get all the contactList where nom does not contain UPDATED_NOM
        defaultContactShouldBeFound("nom.doesNotContain=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllContactsByPrenomIsEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where prenom equals to DEFAULT_PRENOM
        defaultContactShouldBeFound("prenom.equals=" + DEFAULT_PRENOM);

        // Get all the contactList where prenom equals to UPDATED_PRENOM
        defaultContactShouldNotBeFound("prenom.equals=" + UPDATED_PRENOM);
    }

    @Test
    @Transactional
    void getAllContactsByPrenomIsInShouldWork() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where prenom in DEFAULT_PRENOM or UPDATED_PRENOM
        defaultContactShouldBeFound("prenom.in=" + DEFAULT_PRENOM + "," + UPDATED_PRENOM);

        // Get all the contactList where prenom equals to UPDATED_PRENOM
        defaultContactShouldNotBeFound("prenom.in=" + UPDATED_PRENOM);
    }

    @Test
    @Transactional
    void getAllContactsByPrenomIsNullOrNotNull() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where prenom is not null
        defaultContactShouldBeFound("prenom.specified=true");

        // Get all the contactList where prenom is null
        defaultContactShouldNotBeFound("prenom.specified=false");
    }

    @Test
    @Transactional
    void getAllContactsByPrenomContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where prenom contains DEFAULT_PRENOM
        defaultContactShouldBeFound("prenom.contains=" + DEFAULT_PRENOM);

        // Get all the contactList where prenom contains UPDATED_PRENOM
        defaultContactShouldNotBeFound("prenom.contains=" + UPDATED_PRENOM);
    }

    @Test
    @Transactional
    void getAllContactsByPrenomNotContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where prenom does not contain DEFAULT_PRENOM
        defaultContactShouldNotBeFound("prenom.doesNotContain=" + DEFAULT_PRENOM);

        // Get all the contactList where prenom does not contain UPDATED_PRENOM
        defaultContactShouldBeFound("prenom.doesNotContain=" + UPDATED_PRENOM);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age equals to DEFAULT_AGE
        defaultContactShouldBeFound("age.equals=" + DEFAULT_AGE);

        // Get all the contactList where age equals to UPDATED_AGE
        defaultContactShouldNotBeFound("age.equals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsInShouldWork() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age in DEFAULT_AGE or UPDATED_AGE
        defaultContactShouldBeFound("age.in=" + DEFAULT_AGE + "," + UPDATED_AGE);

        // Get all the contactList where age equals to UPDATED_AGE
        defaultContactShouldNotBeFound("age.in=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsNullOrNotNull() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age is not null
        defaultContactShouldBeFound("age.specified=true");

        // Get all the contactList where age is null
        defaultContactShouldNotBeFound("age.specified=false");
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age is greater than or equal to DEFAULT_AGE
        defaultContactShouldBeFound("age.greaterThanOrEqual=" + DEFAULT_AGE);

        // Get all the contactList where age is greater than or equal to UPDATED_AGE
        defaultContactShouldNotBeFound("age.greaterThanOrEqual=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age is less than or equal to DEFAULT_AGE
        defaultContactShouldBeFound("age.lessThanOrEqual=" + DEFAULT_AGE);

        // Get all the contactList where age is less than or equal to SMALLER_AGE
        defaultContactShouldNotBeFound("age.lessThanOrEqual=" + SMALLER_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsLessThanSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age is less than DEFAULT_AGE
        defaultContactShouldNotBeFound("age.lessThan=" + DEFAULT_AGE);

        // Get all the contactList where age is less than UPDATED_AGE
        defaultContactShouldBeFound("age.lessThan=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAgeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where age is greater than DEFAULT_AGE
        defaultContactShouldNotBeFound("age.greaterThan=" + DEFAULT_AGE);

        // Get all the contactList where age is greater than SMALLER_AGE
        defaultContactShouldBeFound("age.greaterThan=" + SMALLER_AGE);
    }

    @Test
    @Transactional
    void getAllContactsByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where address equals to DEFAULT_ADDRESS
        defaultContactShouldBeFound("address.equals=" + DEFAULT_ADDRESS);

        // Get all the contactList where address equals to UPDATED_ADDRESS
        defaultContactShouldNotBeFound("address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllContactsByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where address in DEFAULT_ADDRESS or UPDATED_ADDRESS
        defaultContactShouldBeFound("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS);

        // Get all the contactList where address equals to UPDATED_ADDRESS
        defaultContactShouldNotBeFound("address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllContactsByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where address is not null
        defaultContactShouldBeFound("address.specified=true");

        // Get all the contactList where address is null
        defaultContactShouldNotBeFound("address.specified=false");
    }

    @Test
    @Transactional
    void getAllContactsByAddressContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where address contains DEFAULT_ADDRESS
        defaultContactShouldBeFound("address.contains=" + DEFAULT_ADDRESS);

        // Get all the contactList where address contains UPDATED_ADDRESS
        defaultContactShouldNotBeFound("address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllContactsByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where address does not contain DEFAULT_ADDRESS
        defaultContactShouldNotBeFound("address.doesNotContain=" + DEFAULT_ADDRESS);

        // Get all the contactList where address does not contain UPDATED_ADDRESS
        defaultContactShouldBeFound("address.doesNotContain=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal equals to DEFAULT_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.equals=" + DEFAULT_CODEPOSTAL);

        // Get all the contactList where codepostal equals to UPDATED_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.equals=" + UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsInShouldWork() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal in DEFAULT_CODEPOSTAL or UPDATED_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.in=" + DEFAULT_CODEPOSTAL + "," + UPDATED_CODEPOSTAL);

        // Get all the contactList where codepostal equals to UPDATED_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.in=" + UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsNullOrNotNull() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal is not null
        defaultContactShouldBeFound("codepostal.specified=true");

        // Get all the contactList where codepostal is null
        defaultContactShouldNotBeFound("codepostal.specified=false");
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal is greater than or equal to DEFAULT_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.greaterThanOrEqual=" + DEFAULT_CODEPOSTAL);

        // Get all the contactList where codepostal is greater than or equal to UPDATED_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.greaterThanOrEqual=" + UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal is less than or equal to DEFAULT_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.lessThanOrEqual=" + DEFAULT_CODEPOSTAL);

        // Get all the contactList where codepostal is less than or equal to SMALLER_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.lessThanOrEqual=" + SMALLER_CODEPOSTAL);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsLessThanSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal is less than DEFAULT_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.lessThan=" + DEFAULT_CODEPOSTAL);

        // Get all the contactList where codepostal is less than UPDATED_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.lessThan=" + UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void getAllContactsByCodepostalIsGreaterThanSomething() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        // Get all the contactList where codepostal is greater than DEFAULT_CODEPOSTAL
        defaultContactShouldNotBeFound("codepostal.greaterThan=" + DEFAULT_CODEPOSTAL);

        // Get all the contactList where codepostal is greater than SMALLER_CODEPOSTAL
        defaultContactShouldBeFound("codepostal.greaterThan=" + SMALLER_CODEPOSTAL);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultContactShouldBeFound(String filter) throws Exception {
        restContactMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contact.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].prenom").value(hasItem(DEFAULT_PRENOM)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].codepostal").value(hasItem(DEFAULT_CODEPOSTAL)));

        // Check, that the count call also returns 1
        restContactMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultContactShouldNotBeFound(String filter) throws Exception {
        restContactMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restContactMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingContact() throws Exception {
        // Get the contact
        restContactMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingContact() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        int databaseSizeBeforeUpdate = contactRepository.findAll().size();

        // Update the contact
        Contact updatedContact = contactRepository.findById(contact.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedContact are not directly saved in db
        em.detach(updatedContact);
        updatedContact.nom(UPDATED_NOM).prenom(UPDATED_PRENOM).age(UPDATED_AGE).address(UPDATED_ADDRESS).codepostal(UPDATED_CODEPOSTAL);
        ContactDTO contactDTO = contactMapper.toDto(updatedContact);

        restContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contactDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isOk());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
        Contact testContact = contactList.get(contactList.size() - 1);
        assertThat(testContact.getNom()).isEqualTo(UPDATED_NOM);
        assertThat(testContact.getPrenom()).isEqualTo(UPDATED_PRENOM);
        assertThat(testContact.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testContact.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testContact.getCodepostal()).isEqualTo(UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void putNonExistingContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contactDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(contactDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateContactWithPatch() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        int databaseSizeBeforeUpdate = contactRepository.findAll().size();

        // Update the contact using partial update
        Contact partialUpdatedContact = new Contact();
        partialUpdatedContact.setId(contact.getId());

        partialUpdatedContact.age(UPDATED_AGE);

        restContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedContact))
            )
            .andExpect(status().isOk());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
        Contact testContact = contactList.get(contactList.size() - 1);
        assertThat(testContact.getNom()).isEqualTo(DEFAULT_NOM);
        assertThat(testContact.getPrenom()).isEqualTo(DEFAULT_PRENOM);
        assertThat(testContact.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testContact.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testContact.getCodepostal()).isEqualTo(DEFAULT_CODEPOSTAL);
    }

    @Test
    @Transactional
    void fullUpdateContactWithPatch() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        int databaseSizeBeforeUpdate = contactRepository.findAll().size();

        // Update the contact using partial update
        Contact partialUpdatedContact = new Contact();
        partialUpdatedContact.setId(contact.getId());

        partialUpdatedContact
            .nom(UPDATED_NOM)
            .prenom(UPDATED_PRENOM)
            .age(UPDATED_AGE)
            .address(UPDATED_ADDRESS)
            .codepostal(UPDATED_CODEPOSTAL);

        restContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedContact))
            )
            .andExpect(status().isOk());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
        Contact testContact = contactList.get(contactList.size() - 1);
        assertThat(testContact.getNom()).isEqualTo(UPDATED_NOM);
        assertThat(testContact.getPrenom()).isEqualTo(UPDATED_PRENOM);
        assertThat(testContact.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testContact.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testContact.getCodepostal()).isEqualTo(UPDATED_CODEPOSTAL);
    }

    @Test
    @Transactional
    void patchNonExistingContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, contactDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamContact() throws Exception {
        int databaseSizeBeforeUpdate = contactRepository.findAll().size();
        contact.setId(count.incrementAndGet());

        // Create the Contact
        ContactDTO contactDTO = contactMapper.toDto(contact);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContactMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(contactDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contact in the database
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteContact() throws Exception {
        // Initialize the database
        contactRepository.saveAndFlush(contact);

        int databaseSizeBeforeDelete = contactRepository.findAll().size();

        // Delete the contact
        restContactMockMvc
            .perform(delete(ENTITY_API_URL_ID, contact.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Contact> contactList = contactRepository.findAll();
        assertThat(contactList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
