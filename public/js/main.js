$(function () {
    $('.main-menu-handle').click(function(event) {
        $(this).toggleClass('active');
        $('.header-main-menu').toggleClass('slide');
    });

    $('.header-main').scroolly([
        {
            from: 'doc-top + 122px',
            addClass: 'sticky'
        },
        {
            to: 'doc-top + 1px',
            removeClass: 'sticky'
        }
    ]);

    // if ($('.page-reservation')[0]) {
    //   $('.booking-form').scroolly([
    //
    //     {
    //       to: 'con-top - 60px',
    //       css: {
    //           position: 'relative',
    //           width: '100%',
    //           top: '0',
    //           left: '0',
    //           height: 'auto'
    //       }
    //     },
    //
    //     {
    //         from: 'el-top = vp-top + 60px',
    //         css: {
    //             position: 'fixed',
    //             top: '60px',
    //             left: '27%',
    //             width: '71%',
    //             backgroundColor: 'white',
    //             height: '80px',
    //             overflow: 'hidden',
    //             zIndex: '999'
    //         },
    //         onCheckIn: function (el, rule) {
    //             $('.main-section > h1').css({
    //                 marginBottom: '192px'
    //             });
    //         },
    //         onCheckOut: function (el, rule) {
    //             $('.main-section > h1').css({
    //                 marginBottom: 'inherit'
    //             });
    //         }
    //     }
    //
    //   ], $('.main-section'));
    // }

    if ($('.makeOrder')[0] || $('.booking-form')[0]) {

        Date.parseDate = function( input, format ){
            return moment(input,format).toDate();
        };
        Date.prototype.dateFormat = function( format ){
            return moment(this).format(format);
        };

        $.datetimepicker.setLocale('ru');
        $('#checkIn').datetimepicker({
            format:'YYYY.MM.DD',
            formatDate:'YYYY.MM.DD',
            timepicker:false,
            validateOnBlur: true,
            scrollMonth: false,
            scrollTime: false,
            minDate: 0
        });

        $('#checkIn').change(function(e) {
          console.log(document.querySelector('#checkIn').value);
          var min = document.querySelector('#checkIn').value;
          $('#checkOut').datetimepicker({
              format:'YYYY.MM.DD',
              formatDate:'YYYY.MM.DD',
              timepicker:false,
              validateOnBlur: true,
              scrollMonth: false,
              scrollTime: false,
              minDate: min
          });
        });

        if($('.makeOrder')[0]) {
          var link        = document.querySelector('#makeOrder__btn'),
              checkIn     = document.querySelector('#checkIn'),
              checkOut    = document.querySelector('#checkOut'),
              checkInVal  = checkIn.value,
              checkOutVal = checkOut.value;

          var reservation = function () {

            if (checkIn.value === checkInVal || checkOut.value === checkOutVal) return;

            var changeToday = function (elem) {
              var now = new Date(),
                  nowDay = now.getDate(),
                  nowHours = +now.getHours() < 10 ? '0' + now.getHours() : now.getHours(),
                  nowMin = +now.getMinutes() < 10 ? '0' + now.getMinutes() : now.getMinutes(),
                  nowSec = +now.getSeconds() < 10 ? '0' + now.getSeconds() : now.getSeconds(),
                  input = elem.value.replace(/\./g,''),
                  inputDay = +input.slice(-2);

                  if (nowDay === inputDay) {
                    console.log("day: " + nowDay);
                    now.setMinutes(nowMin + 1);
                    console.log(now);
                    console.log('checkIn: ' + input + nowHours + (+nowMin+ 1 ) + nowSec);
                    input = ( new Date(now.setMinutes(nowMin + 1)).getDate() !== nowDay ) ? input + '23' + '59' + '59' :
                    input + nowHours + (+nowMin+ 1 ) + nowSec;
                    return input;
                  }

                  return input + '000000';

            };

            var from = changeToday(checkIn),
                to   = changeToday(checkOut);
                console.log(from + ' ' + to);

            link.href = 'reservation/' + from + '/' + to;
          };

          link.addEventListener('click', reservation);
        }



    }

    $('.scroll-up').click(function (event) {
        $('body, html').animate({
            scrollTop: 0
        }, 1e3);
    });

    if ($('#map')[0]) {
        var mapCanvas = $('#map')[0];
        var mapOptions = {
            center: new google.maps.LatLng(44.5403, -78.5463),
            zoom: 8,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(mapCanvas, mapOptions);
    }

    if($('#imageGallery')[0]) {
        $('#imageGallery').lightSlider({
            gallery: true,
            item: 1,
            loop: true,
            slideMargin: 0,
            thumbItem: 7,
            auto: true,
            speed: 1200,
            pause: 5000,
            keyPress: true,
            pauseOnHover: true,
            onSliderLoad: function(el) {
                el.lightGallery({
                    selector: '#imageGallery .lslide'
                });
            },
            currentPagerPosition:'left'
        });
    }

    $('#google-map').lightGallery({
        selector: 'this',
        iframeMaxWidth: '80%'
    });

    if ($('#lightgallery')[0]) {
        $('.gallery li').each(function () {
            $(this).hoverdir();
        });

        $('#lightgallery').lightGallery({
            selector: 'a'
        });
    }

});
