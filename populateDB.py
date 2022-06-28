import geopandas as gpd
import random
import time
import os
from datetime import datetime
os.environ["PARSE_API_ROOT"] = "https://parseapi.back4app.com"

# Everything else same as usual

from parse_rest.datatypes import Function, Object, GeoPoint, Date
from parse_rest.connection import register
from parse_rest.query import QueryResourceDoesNotExist
from parse_rest.connection import ParseBatcher
from parse_rest.core import ResourceRequestBadRequest, ParseError
from parse_rest.user import User

APPLICATION_ID=''
REST_API_KEY=''
MASTER_KEY=''

register(APPLICATION_ID, REST_API_KEY, master_key=MASTER_KEY)

def str_time_prop(start, end, time_format, prop):
    """Get a time at a proportion of a range of two formatted times.
    start and end should be strings specifying times formatted in the
    given format (strftime-style), giving an interval [start, end].
    prop specifies how a proportion of the interval to be taken after
    start.  The returned time will be in the specified format.
    """
    stime = time.mktime(time.strptime(start, time_format))
    etime=time.mktime(time.strptime(end, time_format))
    ptime=stime + prop * (etime - stime)
    return time.strftime(time_format, time.localtime(ptime))


def random_date(start, end, prop):
    return str_time_prop(start, end, '%Y-%m-%dT%H:%M:%S', prop)


class TypeOfCrime(Object):
    pass
class Report(Object):
    pass

crimes=list(TypeOfCrime.Query.all())
num_crimes=len(crimes)
users=list(User.Query.all())
num_users=len(users)
bay_area=gpd.read_file("bayArea.shp")
num_points=120
num_shapes_bay_area=bay_area.shape[0]
points=list(gpd.points_from_xy([random.uniform(-122.21,-123.6) for i in range(num_points)],[random.uniform(37.75,38.8) for i in range(num_points)]))
i=0
while i < num_points:
    point_in_the_bay_area=False
    point_in_polygon=bay_area.contains(points[i]) ##get a series with a boolean representing if the point is in determinate polygon
    for j in range(num_shapes_bay_area):
        if point_in_polygon[j] :
            point_in_the_bay_area=True
            break
    if not point_in_the_bay_area:
        points.pop(i)
        num_points-=1
    else:
        i+=1

# print(crimes)
for k in range(num_points):
    point=GeoPoint(longitude=points[k].x,latitude=points[k].y)
    user=users[random.randrange(0, num_users)]
    crime=crimes[random.randrange(0, num_users)]
    description="test"
    rand_date=random_date("2021-06-24T00:00:00", "2022-06-24T00:00:00", random.random())
    date= Date(datetime.strptime(rand_date, "%Y-%m-%dT%H:%M:%S"))
    report = Report()
    report.user=user
    report.typeOfCrime=crime
    report.description=description
    report.location=point
    report.date=date
    report.save()
