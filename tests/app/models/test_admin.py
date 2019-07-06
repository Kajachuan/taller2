from taller2.app.app import db
from taller2.app.models.admin import Admin

class TestAdmin(object):
    def test_new_admin(self):
        admin = Admin(name='MiNombre', crypted_password='mipass')
        assert admin.name == 'MiNombre'
        assert admin.crypted_password == 'mipass'
