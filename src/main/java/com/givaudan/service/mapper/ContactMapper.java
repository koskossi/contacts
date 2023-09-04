package com.givaudan.service.mapper;

import com.givaudan.domain.Contact;
import com.givaudan.service.dto.ContactDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Contact} and its DTO {@link ContactDTO}.
 */
@Mapper(componentModel = "spring")
public interface ContactMapper extends EntityMapper<ContactDTO, Contact> {}
