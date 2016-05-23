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
    from:  from,
    to:    to,
    hotel: {},
    room:  {},
    opt:   []
  };
  
  window.globalFilt = globalFilt;
    
  var stringToMinutes = function(time) {
    return moment.duration(time).asMinutes();
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

      breakfast:    elemAndVal(cat, '[data-breakfast]'),

      eci:          elemAndVal(cat, '[data-eci]'),

      lco:          elemAndVal(cat, '[data-lco]'),

      guestsCnt:    elemAndVal(cat, '[data-guestscnt]', 'number'),
      
      addGuestsCnt: elemAndVal(cat, '[data-addguests]', 'number'),

      roomCnt:      elemAndVal(cat, '[data-roomcnt]', 'number'),

      hotelId:      (cat).dataset.hotelid,

      catId:        (cat).dataset.catid,

      hash:         +$(cat).find(".tariff")[0].dataset.hash

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
      from: elemAndVal(elem, '#checkIn').value.replace(/\./g,'') + '000000',
      to: elemAndVal(elem, '#checkOut').value.replace(/\./g,'') + '000000',
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
    
    globalFilt.from = +filters.from;
    globalFilt.to = +filters.to;
    
    console.timeEnd('collectFilters');
    console.log(filters);
    return filters;
  };

  var stringOpts = function (cat) {
    console.time('stringOpts');

    var opt = collectOpts(cat);

    var data = JSON.stringify({
      'hotelId':          opt.hotelId,
      'catId':            opt.catId,
      'guestsCnt':        opt.guestsCnt.value,
      'addGuestsCnt':     opt.addGuestsCnt.value,
      'tariffGroupsHash': opt.hash,
      'roomCnt':          opt.roomCnt.value,
      'bkf':              opt.breakfast.value,
      'ci':               opt.eci.value,
      'co':               opt.lco.value
    });
    console.log(opt.eci.value);
    
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
        for (;i <= maxCnt; i++) {
          var $option = $('<option>');
          $option.val(i).text(i);
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
    
    
  var askFor = function(from, to, req) {
    return $.ajax(jsRoutes.controllers.Application.category(from, to, req, JSON.stringify(createFiltersReqObj(collectFilters($('.filtering')[0])))));
  };
    
  var usualResponceHandling = function(ask, cat, e){
    
    console.log('usualResponceHandling');
    
    ask.done(function(resp) {
      
      $(e.target).parent().find('.additional-days, .time-no-available').hide();
      $(cat).find('.item-not-available').hide();
    
      (resp.ctrl.eci && e.target.name === 'timeIn') ? $(e.target).parent().find('.eci').show() : $(e.target).parent().find('.eci').hide();
      
      (resp.ctrl.lco && e.target.name === 'timeOut') ? $(e.target).parent().find('.lco').show() : $(e.target).parent().find('.lco').hide();
      
      $(cat).find('input').not('[name="timeIn"], [name="timeOut"]').
        removeAttr('disabled');
      
      if(resp.type === 'basic') {
        setupOpt(resp, cat);
        return;
      }
      
      if(resp.type === 'tariffsRedraw') {
        $('.tariffs').empty().append(resp.html);
        setupOpt(resp, cat);
        return;
      }
      
      if(resp.type === 'fullRedraw') {
        $(cat).replaceWith(resp.html);
        return;
      }
      
      if(resp.type === 'gone') {
        $(cat).remove();
        return;
      }
      
    })
      
  };
    
  var specialResponceHandling = function(ask, cat, e) {
    
    console.log('specialResponceHandling');
    
    ask.done(function(resp) {
      $(e.target).parent().find('.eci, .lco').hide();          
        
      if(resp.type === 'basic' || resp.type === 'tariffsRedraw') {
        $(e.target).parent().find('.cico, .time-no-available').hide();
        $(e.target).parent().find('.additional-days').show();
        
        return;
      }
    
      if(resp.type === 'fullRedraw' || resp.type === 'gone') {
        $(e.target).parent().find('.cico, .additional-days').hide();
        $(e.target).parent().find('.time-no-available').show();
        
        $(cat).find('input').not('[name="timeIn"], [name="timeOut"]').
        attr('disabled', 'true');
        $(cat).find('.item-not-available').show();
        
        return;
      }
      
    })
    
  };

  var changeCat = function (cat) {

    cat = cat || '.category-list';

    $(cat).change(function(e) {
      
      var cat  = $('.category').has(e.target)[0],
          req  = stringOpts(cat),
          from = globalFilt.from;
          to   = globalFilt.to;

      console.log("REQUEST: " + req);
      console.log(e.target.name);
      
      // if(e.target.name !== 'timeIn' && e.target.name !== 'timeOut') {
        
      //   usualResponceHandling(from, to, req, cat, e);
        
      // }
      
      if(e.target.name === 'timeIn' || e.target.name === 'timeOut') {
        
        var hotelCI = stringToMinutes($(cat).parent().data('eci')),
            hotelCO = stringToMinutes($(cat).parent().data('lco')),
            catCI   = stringToMinutes($(cat).find('[name="timeIn"]').val()),
            catCO   = stringToMinutes($(cat).find('[name="timeOut"]').val()),
            from    = (catCI <= hotelCI) ?
              +moment(from, "YYYYMMDD").subtract(1, 'd').format("YYYYMMDD000000") :
              from;
            to      = (catCO >= hotelCO) ?
              +moment(to, "YYYYMMDD").add(1, 'd').format("YYYYMMDD000000") :
              to;
              
              console.log('catCI: ' + catCI + ' catCO: ' + catCO);
            
        if(e.target.name === 'timeIn' && catCI <= hotelCI || e.target.name === 'timeOut' && catCO >= hotelCO) {
          
          specialResponceHandling(askFor(from, to, req), cat, e);
          
        } else { 
          usualResponceHandling(askFor(from, to, req), cat, e);
         }
        
        // if(e.target.name === 'timeIn' && catCI > hotelCI || e.target.name === 'timeOut' && catCO < hotelCO) {
          
        //   usualResponceHandling(from, to, req, cat, e);
          
        // }
        
      } else { 
        usualResponceHandling(askFor(from, to, req), cat, e); 
      }

    });
  };

  var changeFilter = function (selector) {
    console.time('changeFilter');
    $(selector).change(function(e) {
        var container = e.currentTarget,
              req  = createFiltersReqObj(collectFilters(selector)),
              from = +collectFilters(selector).from,
              to   = +collectFilters(selector).to;
              
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

        var min = $('#checkIn').val();
        var max = $('#checkOut').val();
        console.log('checkIn value: ' + min);
        $('#checkOut').datetimepicker({
            format:'YYYY.MM.DD',
            formatDate:'YYYY.MM.DD',
            timepicker:false,
            validateOnBlur: false,
            scrollMonth: false,
            scrollTime: false,
            minDate: min
        });

        $('#checkIn').datetimepicker({
            format:'YYYY.MM.DD',
            formatDate:'YYYY.MM.DD',
            timepicker:false,
            validateOnBlur: false,
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
