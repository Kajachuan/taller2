from http import HTTPStatus
from taller2.app.app import app, db
from taller2.app.models.admin import Admin

client = app.test_client()

admin = Admin(name='soyadmin', crypted_password='gAAAAABcw6AmY9iCIQZX4JEJJtmCKNvfRW-fm_4QyPI4StSxDLWNdngTQXP8Ny8J-OVDvf8fv1HGNOBepDK61TFAUD50IL2wrg==')
admin.save()

class TestAdminsController(object):
    def test_correct_login(self):
        response = client.post('/admin/', data='{"name": "soyadmin", "password": "mipass"}')

        assert response.status_code == HTTPStatus.OK

    def test_wrong_user(self):
        response = client.post('/admin/', data='{"name": "cualquiera", "password": "mipass"}')

        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_wrong_password(self):
        response = client.post('/admin/', data='{"name": "soyadmin", "password": "malpass"}')

        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_correct_logout(self):
        client.post('/admin/', data='{"name": "soyadmin", "password": "mipass"}')
        response = client.delete('/admin/logout/')

        assert response.status_code == HTTPStatus.OK
