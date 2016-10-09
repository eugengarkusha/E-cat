$(function () {
  $('.main-menu-handle').click(function(event) {
    $(this).toggleClass('active');
    $('.header-main-menu').toggleClass('slide');
  });

  setTimeout(function() {
    
    $('.header-main').scroolly([
      {
        from: 'doc-top + 122px',
        addClass: ($('.page-reservation')[0]) ? 'sticky sticky-reservation' : 'sticky'
      },
      {
        to: 'doc-top + 1px',
        removeClass: 'sticky sticky-reservation'
      }
    ]);

    if ($('.filtering')[0]) {
      $('.filtering').scroolly([
        {
        from: 'doc-top + 194px',
        addClass: 'filtering-slide',
        onCheckIn: function($element, rule) {
            $('.filtering-placeholder').show();
        },
        onCheckOut: function($element, rule) {
            $('.filtering-placeholder').hide();
        }
        },
        {
        to: 'doc-top + 194px',
        removeClass: 'filtering-slide'
        }
      ]);
    }
    
  }, 500);

  if ($('.makeOrder')[0] || $('.filtering')[0]) {

    Date.parseDate = function( input, format ){
      // return moment(input,format).toDate();
      return moment.tz(new Date(input), 'Europe/Kiev').toDate();
    };
    Date.prototype.dateFormat = function( format ){
      // return moment(this).format(format);
      return moment.tz(this, 'Europe/Kiev').format(format);
    };

    $.datetimepicker.setLocale('ru');
    $('#checkIn').datetimepicker({
      format:         'YYYY.MM.DD  HH:mm',
      formatDate:     'YYYY.MM.DD  HH:mm',
      formatTime:     'HH.mm',
      timepicker:     true,
      step:           30,
      validateOnBlur: false,
      scrollMonth:    false,
      scrollTime:     false,
      minDate:        0
    });

  var setupDatepicker = function () {

    if ($('#checkIn')[0] && $('#checkOut')[0]) {
      $('#checkIn').change(function(e) {
        console.log('Click on #checkIn');
        var min        = document.querySelector('#checkIn').value;
        var max        = document.querySelector('#checkOut').value;
        var wrappedMin = moment(min, 'YYYYMMDD').add(1, 'd');
        $('#checkOut').datetimepicker({
          format:         'YYYY.MM.DD  HH:mm',
          formatDate:     'YYYY.MM.DD  HH:mm',
          formatTime:     'HH.mm',
          timepicker:     true,
          step:           30,
          validateOnBlur: false,
          scrollMonth:    false,
          scrollTime:     false,
          minDate:        wrappedMin.format()
        });
      });

      $('#checkOut').change(function(e) {
        var min        = document.querySelector('#checkIn').value;
        var max        = document.querySelector('#checkOut').value;
        var wrappedMax = moment(max, 'YYYYMMDD');
        console.log(wrappedMax.toDate());
        $('#checkIn').datetimepicker({
          format:         'YYYY.MM.DD  HH:mm',
          formatDate:     'YYYY.MM.DD  HH:mm',
          formatTime:     'HH.mm',
          timepicker:     true,
          step:           30,
          validateOnBlur: false,
          scrollMonth:    false,
          scrollTime:     false,
          minDate:        0,
          maxDate:        wrappedMax.subtract(1, 'd')
        });
      });
    };

  };

  setupDatepicker();

  }

    // set time HH:MM:SS for FROM if first date is TODAY

  (function () {
    
    if ($('.makeOrder')[0]) {
      var link     = $('#makeOrder__btn'),
          fromInit = $('#checkIn').val(),
          toInit   = $('#checkOut').val();

      var reservation = function () {

        var from = moment($('#checkIn').val(), 'YYYYMMDDHHmmss'),
            to   = moment($('#checkOut').val(), 'YYYYMMDDHHmmss'),
            now  = moment().tz('Europe/Kiev');

        if (fromInit === $('#checkIn').val() 
        || toInit === $('#checkOut').val()
        || from === to) {
          return;
        }

         if (from.format('YYYYMMDD') === now.format('YYYYMMDD')) {
           if (moment().tz('Europe/Kiev').add(1, 'h').format('YYYYMMDD') === from.format('YYYYMMDD')) {
             from = moment().tz('Europe/Kiev').add(1, 'm').format('YYYYMMDDHHmmss');
             to   = to.format('YYYYMMDDHHmmss');
           }
         } else {
           from = from.format('YYYYMMDDHHmmss');
           to   = to.format('YYYYMMDDHHmmss');
         }

         if (from === to) return; // it's not working 

        $(link)[0].href = 'reservation/' + from + '/' + to;

      };

      link[0].addEventListener('click', reservation);
    }

  })()

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

  if ($('#imageGallery')[0]) {
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
