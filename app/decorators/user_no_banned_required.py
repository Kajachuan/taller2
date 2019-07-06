from functools import wraps
from http import HTTPStatus
from datetime import datetime
from flask import redirect, session, jsonify
from ..models.user import User

def user_no_banned_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not session.get('username'):
            return jsonify(message='You must be logged in'), HTTPStatus.UNAUTHORIZED
        else:
            user = User.objects.get(username = session['username'])
            if user.ban_date and user.ban_date > datetime.now():
                return jsonify(message='You are banned until '
                                        + str(user.ban_date)
                                        + ' because '
                                        + user.ban_reason), HTTPStatus.FORBIDDEN
        return f(*args, **kwargs)
    return decorated_function
