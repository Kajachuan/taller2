from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

client.post('/register', data='{"username": "IronMan", "email": "tony@stark.com",\
                              "password": "mipass", "password_confirmation": "mipass"}')

class TestUsersController(object):
    def test_new_user(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.CREATED
        assert response.get_json() == {'message': 'The user has been created'}

    def test_wrong_password_confirmation(self):
        response = client.post('/register',
                               data='{"username": "MiNombre2", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "otropass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The password and the confirmation are not the same'}

    def test_blank_username(self):
        response = client.post('/register',
                               data='{"username": "", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username cannot be blank'}

    def test_invalid_email(self):
        response = client.post('/register',
                               data='{"username": "MiNombre3", "email": "usertest.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The email is invalid. It must be user@domain'}

    def test_short_password(self):
        response = client.post('/register',
                               data='{"username": "MiNombre4", "email": "user@test.com",\
                                      "password": "pw", "password_confirmation": "pw"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The password is too short. It must have at least five characters'}

    def test_duplicate_username(self):
        client.post('/register', data='{"username": "NewUser", "email": "user@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        response = client.post('/register',
                               data='{"username": "NewUser", "email": "user2@test.com",\
                                      "password": "123456", "password_confirmation": "123456"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username already exists'}

    def test_set_profile_user_valid(self):
        response = client.post('/profile', data='{"username" : "IronMan" , "first_name" : "Tony",\
                                                  "last_name" : "Stark"}')
        assert response.status_code == HTTPStatus.OK

    def test_set_profile_user_invalid(self):
        response = client.post('/profile', data='{"username" : "Hulk" , "first_name" : "Tony",\
                                                  "last_name" : "Stark"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_get_profile(self):
        response = client.get('/profile/IronMan')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['first_name'] == 'Tony'
        assert response.get_json()['last_name'] == 'Stark'

    def test_get_profile_invalid_user(self):
        response = client.get('/profile/Hulk')
        assert response.status_code == HTTPStatus.BAD_REQUEST
