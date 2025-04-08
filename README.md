# Тестирование API приложения Stellar Burgers
Данный проект написан на языке Java 11 для тестирования ручек API приложения [Stellar Burgers](https://stellarburgers.nomoreparties.site/)

## Задача 
[Документация API](https://code.s3.yandex.net/qa-automation-engineer/java/cheatsheets/paid-track/diplom/api-documentation.pdf)  

#### Протестировать ручки API для [Stellar Burgers](https://stellarburgers.nomoreparties.site/):  
1. Создание пользователя:
    - создать уникального пользователя;
    - создать пользователя, который уже зарегистрирован;
    - создать пользователя и не заполнить одно из обязательных полей.  
2. Логин пользователя:  
    - логин под существующим пользователем;
    - логин с неверным логином и паролем.
3. Изменение данных пользователя:
    - с авторизацией;
    - без авторизации;
    - Для обеих ситуаций нужно проверить, что любое поле можно изменить. Для неавторизованного пользователя — ещё и то, что система вернёт ошибку.
4. Создание заказа:
    - с авторизацией;
    - без авторизации;
    - с ингредиентами;
    - без ингредиентов;
    - с неверным хешем ингредиентов.
5. Получение заказов конкретного пользователя:
    - авторизованный пользователь;
    - неавторизованный пользователь.

## Используемые библиотеки:
- [Junit4](https://junit.org/junit4/)
- [REST Assured](https://rest-assured.io/)
- [Gson](https://github.com/google/gson)
- [JSON-java](https://github.com/stleary/JSON-java?tab=readme-ov-file)
- [Lombok](https://projectlombok.org/)
- [JavaFaker](https://github.com/DiUS/java-faker)
- [Allure Report](https://allurereport.org/)

## Запуск тестов
Этот проект использует `Maven` для запуска тестов. Запуск тестов выполняется командой:
```bash
$ mvn clean test
```
## Отчёт о выполнении тестов
У Вас должен быть установлен `Allure Report`. Установите его для [своей операционной системы](https://allurereport.org/docs/install/).  

После запуска тестов отчёт сформируется автоматически в папку `target/allure-results`

Для просмотра отчёта выполнения тестов необходимо воспользоваться командой:
```bash
$ allure serve target/allure-results
```
