from flask import Blueprint, request, abort, session
from http import HTTPStatus

try:
    from ..models.user import User
except:
    from models.user import User

sessions = Blueprint('sessions', __name__)

@sessions.route('/login', methods=['POST'])
def login():
    data = request.get_json(force=True)
    username = data['username']
    password = data['password']

    user = User.authenticate(username, password)
    if not user:
        abort(HTTPStatus.BAD_REQUEST)

    return '', HTTPStatus.OK
