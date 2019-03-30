from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort
from cryptography.fernet import Fernet
from ..models.user import User

users = Blueprint('users', __name__)

@users.route('/register', methods=['POST'])
def register():
    data = request.get_json(force=True)
    username = data['username']
    email = data['email']
    password = data['password']
    password_confirmation = data['password_confirmation']

    if password != password_confirmation:
        abort(HTTPStatus.UNAUTHORIZED)

    try:
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        crypted_password = cipher_suite.encrypt(password.encode())
    except KeyError:
        crypted_password = password

    new_user = User(username=username, email=email, crypted_password=crypted_password)
    try:
        new_user.save()
    except:
        abort(HTTPStatus.UNAUTHORIZED)
    return '', HTTPStatus.CREATED
