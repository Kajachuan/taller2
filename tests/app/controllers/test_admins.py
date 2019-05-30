from http import HTTPStatus
from os import environ
from cryptography.fernet import Fernet
from taller2.app.app import app, db
from taller2.app.models.admin import Admin

client = app.test_client()

cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
admin = Admin(name='soyadmin', crypted_password=cipher_suite.encrypt('mipass'.encode()))
admin.save()

class TestAdminsController(object):
    def test_correct_admin_login(self):
        response = client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})

        assert response.status_code == HTTPStatus.FOUND

    def test_wrong_admin_name(self):
        response = client.post('/admin/', data={"name": "cualquiera", "password": "mipass"})

        assert response.status_code == HTTPStatus.FOUND

    def test_wrong_admin_password(self):
        response = client.post('/admin/', data={"name": "soyadmin", "password": "malpass"})

        assert response.status_code == HTTPStatus.FOUND

    def test_correct_admin_logout(self):
        client.post('/admin', data={"name": "soyadmin", "password": "mipass"})
        response = client.post('/admin/logout/')

        assert response.status_code == HTTPStatus.FOUND

    def test_get_login_page(self):
        response = client.get('/admin/')
        assert response.status_code == HTTPStatus.OK

    def test_get_home_page(self):
        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        response = client.get('/admin/home/')
        assert response.status_code == HTTPStatus.OK

    def test_get_statistics_page(self):
        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        response = client.get('/admin/statistics/')
        assert response.status_code == HTTPStatus.OK

    def test_get_users_page(self):
        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        response = client.get('/admin/users/')
        assert response.status_code == HTTPStatus.OK

    def test_redirect_if_not_admin(self):
        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        client.post('/admin/logout/')
        response = client.get('/admin/users/')
        assert response.status_code == HTTPStatus.FOUND

    def test_ban(self):
        client.post('/register',
                     data='{"username": "banUser", "email": "user@test.com",\
                            "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        response = client.post('/admin/ban',
                               data={"username": "banUser", "ban_date": "2020-12-20",
                                     "ban_reason": "a reason"})
        assert response.status_code == HTTPStatus.FOUND
        client.post('/admin/logout/')

        response = client.post('/login', data='{"username": "banUser", "password": "mipass"}')
        assert response.status_code == HTTPStatus.UNAUTHORIZED
        assert response.get_json() == {'message': 'You are banned until 2020-12-20 00:00:00 because a reason'}

    def test_ban_while_logged_in(self):
        client.post('/register',
                    data='{"username": "banUser2", "email": "user@test.com",\
                           "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/login', data='{"username": "banUser2", "password": "mipass"}')

        client.post('/admin/', data={"name": "soyadmin", "password": "mipass"})
        response = client.post('/admin/ban',
                               data={"username": "banUser2", "ban_date": "2020-12-20",
                                     "ban_reason": "a reason"})
        client.post('/admin/logout/')
        response = client.get('/profile/banUser2')

        assert response.status_code == HTTPStatus.UNAUTHORIZED
        assert response.get_json() == {'message': 'You are banned until 2020-12-20 00:00:00 because a reason'}
