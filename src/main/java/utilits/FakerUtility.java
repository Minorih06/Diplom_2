package utilits;

import com.github.javafaker.Faker;

public class FakerUtility {
    private Faker faker = new Faker();

    public String getEmailAddress() {
        return faker.internet().emailAddress();
    }

    public String getPassword() {
        return faker.internet().password();
    }

    public String getFirstName() {
        return faker.name().firstName();
    }

    public int getHashCode() {
        return faker.hashCode();
    }
}
