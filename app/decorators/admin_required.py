from functools import wraps
from flask import redirect, session

def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not session.get('admin'):
            return redirect('/admin/')
        return f(*args, **kwargs)
    return decorated_function
