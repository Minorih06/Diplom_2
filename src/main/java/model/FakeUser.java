package model;

import lombok.Getter;
import utilits.FakerUtility;

@Getter
public class FakeUser {
    FakerUtility fakerUtility = new FakerUtility();

    private final String email = fakerUtility.getEmailAddress();
    private final String password = fakerUtility.getPassword();
    private final String name = fakerUtility.getFirstName();
}
