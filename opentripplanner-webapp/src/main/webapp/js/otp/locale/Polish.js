/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.locale");

/**
  * @class
  */
otp.locale.Polish = {

    config : 
    {
        metricsSystem : "international",
        rightClickMsg : "TODO - localize me - Right-click on the map to designate the start and end of your trip.",
        attribution   : {
            title   : "License Attribution",
            content : "Disclaimer goes here"
        }
    },

    contextMenu : 
    {
        fromHere         : "Rozpocznij podr� tutaj",
        toHere           : "Zako�cz podr� tutaj",

        centerHere       : "Centruj map� tutaj",
        zoomInHere       : "Przybli� tutaj",
        zoomOutHere      : "Oddal st�d",
        previous         : "Poprzednia pozycja na mapie",
        next             : "Nast�pna pozycja na mapie"
    },

    service : 
    {
        weekdays:  "Dni robocze",
        saturday:  "Sobota",
        sunday:    "Niedziela",
        schedule:  "Rozk�ad"
    },

    indicators : 
    {
        ok         : "OK",
        date       : "Data",
        loading    : "�adowanie",
        searching  : "Szukanie...",
        qEmptyText : "Adres, skrzy�owanie, obiekt lub ID przystanku..."
    },

    buttons: 
    {
        reverse       : "Odwr��",
        reverseTip    : "<b>Odwr�� kierunki</b><br/>Zaplanuj podr� powrotn� poprzez zamian� miejscami punktu startowego i ko�cowego podr�y i przeskok czasu do przodu.",
        reverseMiniTip: "Odwr�� kierunki",

        edit          : "Edytuj",
        editTip       : "<b>Edytuj podr�</b><br/>Powr�� do planowania podr�y z detalami tej podr�y.",

        clear         : "Wyczy��",
        clearTip      : "<b>Wyczy��</b><br/>Wyczy�� map� i wszystkie aktywne narz�dzia.",

        fullScreen    : "Pe�en ekran",
        fullScreenTip : "<b>Pe�en ekran</b><br/>Poka� lub ukryj panele narz�dzi",

        print         : "Drukuj",
        printTip      : "<b>Drukuj</b><br/>Wydrukuj plan podr�y (bez mapy).",

        link          : "Link",
        linkTip      : "<b>Link</b><br/>Poka� link do tego planu podr�y.",

        feedback      : "Opinie",
        feedbackTip   : "<b>Opinie</b><br/>Wy�lij swoje uwagi i do�wiadczenia z narz�dzia",

        submit       : "Wy�lij",
        clearButton  : "Wyczy��",
        ok           : "OK",
        cancel       : "Anuluj",
        yes          : "Tak",
        no           : "Nie"
    },

    // note: keep these lower case (and uppercase via template / code if needed)
    directions : 
    {
        southEast:      "po�udniowy wsch�d",
        southWest:      "po�udniowy zach�d",
        northEast:      "p�nocny wsch�d",
        northWest:      "p�nocny zach�d",
        north:          "p�noc",
        west:           "zach�d",
        south:          "po�udnie",
        east:           "wsch�d",
        bound:          "w kierunku",
        left:           "lewo",
        right:          "prawo",
        slightly_left:  "lekko w lewo",
        slightly_right: "lekko w prawo",
        hard_left:      "mocno w lewo",
        hard_right:     "mocno w prawo",
        'continue':     "kontynuuj",
        to_continue:    "kontynuowa�",
        becomes:        "zmienia si� w",
        at:             "o"
    },

    time:
    {
        minute_abbrev:  "min",
        minutes_abbrev: "minut",
        second_abbrev: "sek",
        seconds_abbrev: "sekund",
        months:         ['Sty', 'Lut', 'Mar', 'Kwi', 'Maj', 'Cze', 'Lip', 'Sie', 'Wrz', 'Paz', 'Lis', 'Gru']
    },
    
    systemmap :
    {
        labels :
        {
            panelTitle : "Mapa systemowa"
        }
    },

    tripPlanner :
    {
        labels : 
        {
            panelTitle    : "Planer podr�y",
            tabTitle      : "Zaplanuj podr�",
            inputTitle    : "Szczeg�y podr�y",
            optTitle      : "Preferencje podr�y (opcjonalne)",
            submitMsg     : "Planuje Twoj� podr�...",
            optionalTitle : "",
            date          : "Data",
            time          : "Godzina",
            when          : "Kiedy",
            from          : "Z",
            fromHere      : "Sk�d",
            to            : "Do",
            toHere        : "Dok�d",
            minimize      : "Poka�",
            maxWalkDistance: "Maksymalny spacer",
            arriveDepart  : "Dojazd/odjazd o",
            mode          : "Podr�uj",
            wheelchair    : "Podr� dost�pna dla niepe�nosprawnych", 
            go            : "Id�",
            planTrip      : "Planuj swoj� podr�",
            newTrip       : "Nowa podr�"
        },

        // see otp/config.js for where these values are used
        link : 
        {
            text           : "Link to this trip (OTP)",
            trip_separator : "This trip on other transit planners",
            bike_separator : "On other bike trip planners",
            walk_separator : "On other walking direction planners",
            google_transit : "Google Transit",
            google_bikes   : "Google Bike Directions",
            google_walk    : "Google Walking Directions",
            google_domain  : "http://www.google.com"
        },

        error:
        {
            title        : 'B�ad planera podr�y',
            deadMsg      : "Planer podr�y nie odpowiada. Odczekaj kilka minut i spr�buj ponownie, lub spr�buj wersji tekstowej planera (zobacz link poni�ej).",
            geoFromMsg   : "Wybierz lokalizacj� 'Z' dla Twojej podr�y: ",
            geoToMsg     : "Wybierz lokalizacj� 'Do' dla Twojej podr�y: "
        },
        
        // default messages from server if a message was not returned
        msgcodes:
        {
            200: "Plan OK",
            500: "B��d serwera",
            400: "Podr� poza obs�ugiwanym obszarem",
            404: "Trasa nieodnaleziona",
            406: "Brak czas�w w rozk�adzie",
            408: "Limit czasu osi�gni�ty",
            413: "Niew�a�ciwy parametr",
            440: "Geokod Z nieodnaleziony",
            450: "Geokod Do nieodnaleziony",
            460: "Geokody Z i Do nieodnalezione",
            409: "Zbyt blisko",
            340: "Geokod Z niejednoznaczny",
            350: "Geokod Do niejednoznaczny",
            360: "Geokody Z i Do niejednoznaczne"
        },

        options: 
        [
          ['TRANSFERS', 'Ma�o przesiadek'],
          ['QUICK',     'Najszybsza podr�'],
          ['SAFE',      'Najbezpieczniejsza podr�']
        ],
    
        arriveDepart: 
        [
          ['false', 'Odjazd'], 
          ['true',  'Przyjazd']
        ],
    
        maxWalkDistance : 
        [
            ['200',   '200 m'],
            ['500',   '500 m'],
            ['1000',   '1 km'],
            ['1500',  '1,5 km'],
            ['5000',  '5 km'],
            ['10000',  '10 km'],
        ],
    
        mode : 
        [
            ['TRANSIT,WALK', 'Transport publiczny'],
            ['BUSISH,TRAINISH,WALK', 'Autobus i tramwaj'],
            ['BUSISH,WALK', 'Tylko autobus'],
            ['TRAINISH,WALK', 'Tylko tramwaj'],
            ['WALK', 'Tylko spacer'],
            ['BICYCLE', 'Rower'],
            ['TRANSIT,BICYCLE', 'Transport publiczny i rower']
        ],

        wheelchair :
        [
            ['false', 'Niewymagane'],
            ['true', 'Wymagane']
        ]
    },

    CLASS_NAME : "otp.locale.Polish"
};
