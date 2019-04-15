from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort, jsonify
from cryptography.fernet import Fernet

try:
    from ..models.user import User
except:
    from models.user import User

users = Blueprint('users', __name__)

@users.route('/register', methods=['POST'])
def register():
    data = request.get_json(force=True)
    username = data['username']
    email = data['email']
    password = data['password']
    password_confirmation = data['password_confirmation']

    if len(password) < 5 or password != password_confirmation:
        abort(HTTPStatus.BAD_REQUEST)

    try:
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        crypted_password = cipher_suite.encrypt(password.encode())
    except KeyError:
        crypted_password = password

    new_user = User(username=username, email=email, crypted_password=crypted_password)

    try:
        new_user.save()
    except:
        abort(HTTPStatus.BAD_REQUEST)

    return '', HTTPStatus.CREATED

@users.route('/profile', methods=['POST'])
def profile():
    data = request.get_json(force=True)
    username = data['username']
    first_name = data['first_name']
    last_name = data['last_name']

    user = User.objects(username = username)
    if not user:
        abort(HTTPStatus.BAD_REQUEST)

    user.update_one(first_name = first_name, last_name = last_name)
    return '', HTTPStatus.OK

@users.route('/profile/<username>', methods = ['GET'])
def get_profile(username):
    user = User.objects.get(username = username)
    return jsonify(first_name = user.first_name, last_name = user.last_name), HTTPStatus.OK
