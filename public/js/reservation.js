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
      console.log(selector);
      var result = {};
      result.element = container.querySelector(selector);
      
      if (!result.element) {
        result.value = 0;
        console.log(result);
        return result;
      }

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
        
        addGuestsCnt:  elemAndVal(cat, '[data-addguests]', 'number'),

        roomCnt: elemAndVal(cat, '[data-roomcnt]', 'number'),

        hotelId: (cat).dataset.hotelid,

        catId: (cat).dataset.catid,

        hash: +(cat).dataset.hash

      };

      globalFilt.hotel = {
        name: elemAndVal($('.filtering')[0], '#hotel').value
      };
      if (globalFilt.hotel.name === '') delete globalFilt.hotel.name;

      globalFilt.hotel = JSON.stringify(globalFilt.hotel);


      globalFilt.room = {
        twin: elemAndVal($('.filtering')[0], '#twin').value,
        guests: elemAndVal($('.filtering')[0], '#peopleQuantity', 'number').value
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
          'hotelId':                  opt.hotelId,
          'catId':                     opt.catId,
          'tariffGroupsHash': opt.hash,
          'guestsCnt':             opt.guestsCnt.value,
          'addGuestsCnt':        opt.addGuestsCnt.value,
          'roomCnt':               opt.roomCnt.value,
           'twinRequired':       elemAndVal($('.filtering')[0], '#twin').value,
          'bkf':                         opt.breakfast.value,
          'ci':                           opt.eci.value,
          'co':                          opt.lco.value
      });
      console.timeEnd('stringOpts');
      return data;

    };

    // ***************** Setup Options ***************************

    var changeSelect = function (cat, elem, maxCnt) {
      console.time('changeSelect');
      
      if (maxCnt === 0 && $(cat).find(elem).find('option').length !== 0) {
        $(cat).find('option-add-guest').hide();
      }

      if (maxCnt !== 0 && $(cat).find(elem).find('option').length !== maxCnt || 
      maxCnt !== 0 && $(cat).find(elem).find('option').length === 0) {
        (function () {
          var i       = 0,
              $select = $(cat).find(elem),
              value   = $select.val() || maxCnt;
          $select.html('');
          for (;i < maxCnt; i++) {
            var $option = $('<option>');
            $option.val(i + 1).text(i + 1);
            $select.append($option);
          }

          $select.val(value);
          
          if (maxCnt !== 0 && $(cat).find(elem).find('option').length === 0) {
            $(cat).find('option-add-guest').show();
          }

          console.timeEnd('changeSelect');
        })();
      }

    };

    var setupOpt = function (data, cat) {
      console.time('setupOpt');

      changeSelect(cat, '[name="guest"]', data.ctrl.maxGuestCnt);
      
      changeSelect(cat, '[name="addGuest"]', data.ctrl.maxAddGuesstsCnt);

      changeSelect(cat, '[name="room_count"]', data.ctrl.availableRoomCnt);
      
      for (var key in data.ctrl.prices) {
        $(cat).find('[data-tariff-name=' + key + ']').find('.tariff-price').text(data.ctrl.prices[key] + ' грн');
      }
      
      (data.ctrl.eci) ? $(cat).find('.eci').show() : $(cat).find('.eci').hide();
      (data.ctrl.lco) ? $(cat).find('.lco').show() : $(cat).find('.lco').hide();

      console.timeEnd('setupOpt');

    };
    
    var createFiltersReqObj = function (opt) {
      
      var result  =  {
        
        name: {
          op : "EQ",
          v : opt.hotel.name
        },
        categories : {
            elFilter: {
              rooms: {
                elFilter: {
                  guestsCnt: {
                    op: "GTEQ",
                    v: opt.room.guests
                  },
                  twin: {
                    op: "EQ",
                    v: opt.room.twin
                  }
                }
            }
          }
        }
        
      };
      
      if (!opt.hotel.name) delete result.name;
      if (!opt.room.twin) delete result.categories.elFilter.rooms.elFilter.twin;
      
      return result;
      
    };

    // ***************************End of setup options***********************

    var changeCat = function (cat) {
      console.time('changeCat');

      cat = cat || '.category-list';

      $(cat).change(function(e) {
          var cat     = $('.category').has(e.target)[0],
              req = stringOpts(cat);

            console.log(req);
            console.log(globalFilt);

        $.ajax(jsRoutes.controllers.Application.category(from, to, req))
        .done(function( resp ) {

          resp.changed ? (function () {
            alert('Категория изменилась!');
            var category = resp.categoryHtml;
            $(cat).replaceWith(category);
            console.log($(category).attr('data-catid'));
            changeCat('[data-catid=' + $(category).attr('data-catid') + ']');
          })() : (function () {
              console.log(resp);

              setupOpt(resp, cat);
          })()

          //  beautySelect();
           console.timeEnd('changeCat');
        });

      });
    };

    var changeFilter = function (selector) {
      console.time('changeFilter');
      $(selector).change(function(e) {
          var container = e.currentTarget,
                req           = createFiltersReqObj(collectFilters(selector)),
                from        = collectFilters(selector).from(),
                to            = collectFilters(selector).to();
                
                console.log(req);

            $.ajax(jsRoutes.controllers.Application.filter(from, to, JSON.stringify(req)))
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
        $('.filtering').after(resp);

        $('#checkIn').val(moment((from).toString().slice(0,-6)).format('YYYY.MM.DD'));
        $('#checkOut').val(moment((to).toString().slice(0,-6)).format('YYYY.MM.DD'));

        beautySelect();

        changeCat('.category-list');

        changeFilter($('.filtering')[0]);


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
