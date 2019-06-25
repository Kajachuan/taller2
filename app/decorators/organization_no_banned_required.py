from functools import wraps
from http import HTTPStatus
from datetime import datetime
from flask import redirect, session, jsonify
from ..models.organization import Organization

def organization_no_banned_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        organization_name = kwargs['organization_name']
        organization = Organization.objects.get(organization_name=organization_name)
        if organization.ban_date and organization.ban_date > datetime.now():
            return jsonify(message='This organization is banned until '
                                    + str(organization.ban_date)
                                    + ' because '
                                    + organization.ban_reason), HTTPStatus.FORBIDDEN
        return f(*args, **kwargs)
    return decorated_function
