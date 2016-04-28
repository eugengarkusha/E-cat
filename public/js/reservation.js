$(function () {

  var beautySelect = function () {
    $('select').selectric({
      responsive: true
    });
  };

  var categoryGallery = function () {
    if ($('.category-gallery')[0]) {

    $('.category-gallery ul').each(function(index, elem) {
      var elemID = 'gallery#' + index;
      elem.id = elemID;
      $(elem).lightSlider({
        gallery: true,
        item: 1,
        slideMargin: 0,
        thumbItem: 7,
        keyPress: true,
        onSliderLoad: function(el) {
          el.lightGallery({
            selector: '#' + elemID + ' .lslide'
          });
        },
        currentPagerPosition:'left'
      });
    });

  }
  };

  var categoryTimepicker = function () {
    $('.timepicker').timepicker({
      timeFormat: 'H:i',
      disableTextInput: true
    });
  };

  var tariffsOpen = function () {
    if ($('.tariffs-btn')[0]) {
      $('.tariffs-btn').click(function (e) {
        $(e.currentTarget).parent().addClass('tariffs-open');
      });
    }
  };

  var globalFilt = {
    hotel: {},
    room: {},
    opt: []
  };

    var elemAndVal = function (container, selector, valueType) {
      console.time('elemAndVal');
      var result = {};
      result.element = container.querySelector(selector);

      result.value = (function () {
        if (result.element.type === 'checkbox') {
          console.timeEnd('elemAndVal');
          return result.element.checked ? true : false;
        }

        if(result.element.tagName === "SELECT" || result.element.tagName === "INPUT") {
          console.timeEnd('elemAndVal');
          return result.element.value;
        }

        console.timeEnd('elemAndVal');
        return result.element.text;

      })();

      if (valueType === 'number') {
        result.value = +result.value;
      }

      console.timeEnd('elemAndVal');

      return result;

    };

    var collectOpts = function (cat) {
      console.time('collectOpts');

      var options = {

        breakfast: elemAndVal(cat, '[data-breakfast]'),

        eci: elemAndVal(cat, '[data-eci]'),

        lco: elemAndVal(cat, '[data-lco]'),

        guestsCnt: elemAndVal(cat, '[data-guestscnt]', 'number'),

        roomCnt: elemAndVal(cat, '[data-roomcnt]', 'number'),

        price: elemAndVal(cat, '[data-price]', 'number'),

        hotelId: (cat).dataset.hotelid,

        catId: (cat).dataset.catid,

        hash: +(cat).dataset.hash

      };

      globalFilt.hotel = {
        name: elemAndVal($('.booking-form')[0], '#hotel').value
      };
      if (globalFilt.hotel.name === '') delete globalFilt.hotel.name;

      globalFilt.hotel = JSON.stringify(globalFilt.hotel);


      globalFilt.room = {
        twin: elemAndVal($('.booking-form')[0], '#twin').value,
        guests: elemAndVal($('.booking-form')[0], '#peopleQuantity', 'number').value
      };
      if (globalFilt.room.twin === false) delete globalFilt.room.twin;

      globalFilt.room = JSON.stringify(globalFilt.room);

      console.log(JSON.stringify(globalFilt.room));
      globalFilt.opt = [];

      globalFilt.opt = JSON.stringify(globalFilt.opt);

      console.timeEnd('collectOpts');
      return options;

    };

    var collectFilters = function (elem) {
      console.time('collectFilters');
      var filters = {
        from: function () {
          return elemAndVal(elem, '#checkIn').value.replace(/\./g,'') + '000000';
        },
        to: function () {
          return elemAndVal(elem, '#checkOut').value.replace(/\./g,'') + '000000';
        },
        hotel: {
          name: elemAndVal(elem, '#hotel').value
        },
        room: {
          twin: elemAndVal(elem, '#twin').value,
          guests: elemAndVal(elem, '#peopleQuantity', 'number').value
        }
      };
      if (filters.hotel.name === '') delete filters.hotel.name;
      if (filters.room.twin === false) delete filters.room.twin;
      console.timeEnd('collectFilters');
      console.log(filters);
      return filters;
    };

    var stringOpts = function (cat) {
      console.time('stringOpts');

      var opt = collectOpts(cat);

      var data = JSON.stringify({
          'hotelId'  : opt.hotelId,
          'catId'    : opt.catId,
          'hash'     : opt.hash,
          'guestsCnt': opt.guestsCnt.value,
          'roomCnt'  : opt.roomCnt.value,
          'bkf'      : opt.breakfast.value,
          'eci'      : opt.eci.value,
          'lco'      : opt.lco.value
      });
      console.timeEnd('stringOpts');
      return data;

    };

    // ***************** Setup Options ***************************

    var changeSelect = function (container, elem, maxCnt) {
      console.time('changeSelect');

      if ($(container).find(elem + 'option').length !== maxCnt) {
        (function () {
          var i       = 0,
              $select = $(container).find(elem),
              value   = $select.val();
          $select.html('');
          for (;i < maxCnt; i++) {
            var $option = $('<option>');
            $option.val(i + 1).text(i + 1);
            $select.append($option);
          }

          $select.val(value);

          console.timeEnd('changeSelect');
        })();
      }

    };

    var setupOpt = function (data, cat) {
      console.time('setupOpt');

      changeSelect(cat, '[data-guestscnt]', data.maxGuestCnt);

      changeSelect(cat, '[data-roomcnt]', data.maxRoomCnt);

      console.timeEnd('setupOpt');

    };

    // ***************************End of setup options***********************

    var changeCat = function (cat) {
      console.time('changeCat');

      cat = cat || '.category-list';

      $(cat).change(function(e) {
          var cat     = $('.category').has(e.target)[0],
              reqData = stringOpts(cat);

            console.log(reqData);
            console.log(globalFilt);

        $.ajax(jsRoutes.controllers.Application.category(from, to, reqData, globalFilt.hotel, globalFilt.room, globalFilt.opt))
        .done(function( resp ) {

          resp.changed ? (function () {
            alert('Категория изменилась!');
            var category = resp.categoryHtml;
            $(cat).replaceWith(category);
            console.log($(category).attr('data-catid'));
            changeCat('[data-catid=' + $(category).attr('data-catid') + ']');
          })() :
          $(cat).find('[data-price]').text(resp.price);

          console.log(resp.price);

           setupOpt(resp, cat);

           beautySelect();
           console.timeEnd('changeCat');
        });

      });
    };

    var changeFilter = function (selector) {
      console.time('changeFilter');
      $(selector).change(function(e) {
          var container = e.currentTarget,
              req       = collectFilters(selector);

            console.log(req.from() + " : " + req.to() + " : " + JSON.stringify(req.hotel) + " : " + JSON.stringify(req.room) + " : " + '[]');

            $.ajax(jsRoutes.controllers.Application.filter(req.from(), req.to(), JSON.stringify(req.hotel), JSON.stringify(req.room), '[]'))
            .done(function( response ) {

              $('.category-list').replaceWith(response);

              categoryGallery();

              categoryTimepicker();

              tariffsOpen();

              beautySelect();

              changeCat();
              console.timeEnd('changeFilter');
            });
      });
    };

//   ************************Starting dynamic****************************

    $.ajax(jsRoutes.controllers.Application.getDummyOffers(from, to))
      .done(function( resp ) {
        $('.booking-form').after(resp);

        $('#checkIn').val(moment((from).toString().slice(0,-6)).format('YYYY.MM.DD'));
        $('#checkOut').val(moment((to).toString().slice(0,-6)).format('YYYY.MM.DD'));

        beautySelect();

        changeCat('.category-list');

        changeFilter($('.booking-form')[0]);


        (function () {

          var min = $('#checkIn').val();
          var max = $('#checkOut').val();
          console.log('checkIn value: ' + min);
          $('#checkOut').datetimepicker({
              format:'YYYY.MM.DD',
              formatDate:'YYYY.MM.DD',
              timepicker:false,
              validateOnBlur: true,
              scrollMonth: false,
              scrollTime: false,
              minDate: min
          });

          $('#checkIn').datetimepicker({
              format:'YYYY.MM.DD',
              formatDate:'YYYY.MM.DD',
              timepicker:false,
              validateOnBlur: true,
              scrollMonth: false,
              scrollTime: false,
              minDate: 0,
              maxDate: max
          });

          categoryGallery();

          categoryTimepicker();

          tariffsOpen();

        })();

    });
});
