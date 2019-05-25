from http import HTTPStatus
from datetime import date, timedelta
from flask import Blueprint, jsonify
from ..models.user import User

statistics = Blueprint('statistics', __name__)

def get_period_statistics(queryset, days):
    period = date.today() - timedelta(days=days)
    queryset = queryset(creation_date__gte=period)
    pipeline = [
        { '$project': {
            'date': { '$dateToString': { 'format': '%d-%m-%Y', 'date': '$creation_date' } }
        } },
        { '$group': {
            '_id': '$date',
            'count' : { '$sum': 1 }
        } }
    ]
    return list(queryset.aggregate(*pipeline))

@statistics.route('/statistics/users', methods=['GET'])
def users_statistics():
    data = {}
    user_queryset = User.objects
    data['count'] = user_queryset.count()
    data['week'] = get_period_statistics(user_queryset, 7)
    data['month'] = get_period_statistics(user_queryset, 31)
    return jsonify(data), HTTPStatus.OK
