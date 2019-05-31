from http import HTTPStatus
from os import environ
from cryptography.fernet import Fernet
from taller2.app.app import app, db
from taller2.app.models.admin import Admin
from taller2.app.models.forbidden_words import ForbiddenWords

client = app.test_client()

cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
admin = Admin(name='soyadmin', crypted_password=cipher_suite.encrypt('mipass'.encode()))
admin.save()
ForbiddenWords().save()

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

    def test_get_forbidden_words_page(self):
        response = client.get('/admin/forbidden-words/')
        assert response.status_code == HTTPStatus.OK

    def test_add_forbidden_word(self):
        response = client.post('/admin/forbidden-words/words', data={"word":"ptm"})
        assert response.status_code == HTTPStatus.FOUND
        response = client.get('/admin/forbidden-words/words')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['list_of_words'] == ['ptm']

    def test_delete_forbidden_word(self):
        response = client.post('/admin/forbidden-words/word-delete', data={"word":"ptm"})
        assert response.status_code == HTTPStatus.FOUND
        response = client.get('/admin/forbidden-words/words')
        assert response.get_json()['list_of_words'] == []
