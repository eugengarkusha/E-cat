$(function () {

  var beautySelect = function (selector) {
    $(selector).find('select').selectric({
      responsive: true
    });
  };

  var categoryGallery = function (selector) {
    $(selector).find('.category-gallery ul').each(function(index, elem) {
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
  };

  var categoryTimepicker = function (from) {
    var from = '' + from;
    var time = moment.tz(from, 'Europe/Kiev').format('YYYYMMDD');
    var today = moment.tz(new Date(), 'Europe/Kiev').format('YYYYMMDD');
    // console.log('FROM',from ,'TIME', time, 'TODAY', today);
    $('.timepicker').timepicker({
      timeFormat: 'H:i',
      disableTextInput: true
    });
    if (time === today) {
      console.log('BIngo');
      var minTime = moment.tz(new Date(), 'Europe/Kiev');
      var minutes = minTime.minutes();
      var ci = $('.timepicker.ci') ;
      if (minutes > 30) {
        minTime.add(60 - minutes, 'm');
      } else if (minutes < 30) {
        minTime.add(30 - minutes, 'm');
      }
      ci.timepicker({
        timeFormat: 'H:i',
        disableTextInput: true,
        'minTime': minTime.format('HH:mm'),
        'maxTime': '23:30'
      });
      ci.val(minTime.format('HH:mm'));
    } 
  };

  var setupFrom = function (from) {
    var now = moment().tz('Europe/Kiev');
    var from = from;
    if (from.format('YYYYMMDD') === now.format('YYYYMMDD')) {
      if (moment().tz('Europe/Kiev').add(1, 'h').format('YYYYMMDD') === from.format('YYYYMMDD')) {
        from = moment().tz('Europe/Kiev').add(1, 'm').format('YYYYMMDDHHmmss');
      } else {
        from = moment().tz( 'Europe/Kiev').add(1, 'd').format('YYYYMMDD000000');
      }
    } else {
      from = from.format('YYYYMMDDHHmmss');
    }

    return from;
  };

  var globalFilt = {
    from:  moment((from), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss'),
    to:    moment((to), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss'),
    hotel: {},
    room:  {},
    opt:   []
  };
      
  var stringToMinutes = function(time) {
    return moment.duration(time).asMinutes();
  };

  var elemAndVal = function (container, selector, valueType) {
    // console.time('elemAndVal');
    var result = {};
    result.element = container.querySelector(selector);
    
    if (!result.element) {
      result.value = 0;
      return result;
    }

    result.value = (function () {
      if (result.element.type === 'checkbox') {
        console.timeEnd('elemAndVal');
        return result.element.checked ? true : false;
      }

      if (result.element.tagName === "SELECT" || result.element.tagName === "INPUT") {
        console.timeEnd('elemAndVal');
        return result.element.value;
      }

      console.timeEnd('elemAndVal');
      return result.element.text;

    })();

    if (valueType === 'number') {
      result.value = +result.value;
    }

    // console.timeEnd('elemAndVal');

    return result;

  };

  var collectOpts = function (cat) {
    console.time('collectOpts');
    
    var roomOpts = $(cat).find('.cat-settings-item').map(function (index, elem) {
     return {
       id: +elem.dataset.tabviewid,
       guestsCnt: elemAndVal(elem, '[data-guestscnt]', 'number').value,
       addGuestsCnt: elemAndVal(elem, '[data-addguests]', 'number').value,
       twin: !!elemAndVal(elem, '[data-twin]').value,
       bkf: !!elemAndVal(elem, '[data-breakfast]').value
     }; 
    }).get();

    var options = {

      eci:          elemAndVal(cat, '[data-eci]'),

      lco:          elemAndVal(cat, '[data-lco]'),

      roomCnt:      elemAndVal(cat, '[data-roomcnt]', 'number'),

      hotelId:      (cat).dataset.hotelid,

      catId:        (cat).dataset.catid,
      
      roomReqs:     roomOpts,

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

    globalFilt.opt = [];

    globalFilt.opt = JSON.stringify(globalFilt.opt);

    console.timeEnd('collectOpts');
    
    return options;

  };

  var collectFilters = function (elem) {
    console.time('collectFilters');
    // console.log(moment(elemAndVal(elem, '#checkIn').value, 'YYYYMMDD'));
    var filters = {
      from:     moment(elemAndVal(elem, '#checkIn').value, 'YYYYMMDDHHmmss'),
      to:       moment(elemAndVal(elem, '#checkOut').value, 'YYYYMMDDHHmmss'),
      ci:       moment(elemAndVal(elem, '#checkIn').value, 'YYYYMMDDHHmmss').format('HH:mm'),
      co:       moment(elemAndVal(elem, '#checkOut').value, 'YYYYMMDDHHmmss').format('HH:mm'),
      hotel:    {
        name:   elemAndVal(elem, '#hotel').value
      },
      room:     {
        twin:   elemAndVal(elem, '#twin').value,
        guests: elemAndVal(elem, '#peopleQuantity', 'number').value
      }
    };
    if (filters.hotel.name === '') delete filters.hotel.name;
    if (filters.room.twin === false) delete filters.room.twin;
    
    globalFilt.from = filters.from;
    globalFilt.to   = filters.to;
    globalFilt.ci   = filters.ci;
    globalFilt.co   = filters.co;
    
    console.timeEnd('collectFilters');
    return filters;
  };

  var stringOpts = function (cat) {
    console.time('stringOpts');

    var opt = collectOpts(cat);
    // console.log(opt.roomReqs);

    var data = JSON.stringify({
      'hotelId':          opt.hotelId,
      'catId':            opt.catId,
      'roomReqs':         opt.roomReqs,
      'tariffGroupsHash': opt.hash,
      'ci':               moment($('#checkIn').val(), 'YYYYMMDDHHmmss').format('HH:mm'),
      'co':               moment($('#checkOut').val(), 'YYYYMMDDHHmmss').format('HH:mm')
    });
    
    console.timeEnd('stringOpts');
    return data;

  };
  
  var tariffSum = function (category) {
    // console.log('tariffSum');
    $(category).each(function (index, cat) {
      var sum = 0;
      $(cat).find('.tariff').each(function (index, tariff) {
        if ($(tariff).find('[name="tariff-btn"]')[0].hasAttribute('checked')) {
          sum += +$(tariff).find('[name="tariff-price"]').val();
        }
      });
    $(cat).find('[data-overall]').text(sum);
    })
  };

  var switchTariff = function (cat, tariffBtn) {
    var view = $(cat).find('.cat-settings-item').has(tariffBtn);
    var radio = view.find('.tariff').find(tariffBtn).clone();
    radio.attr('checked', 'checked');
    view.find('.tariff .tariff-radio').removeAttr('checked');
    view.find('.tariff').find(tariffBtn).replaceWith(radio);
    tariffSum(cat);
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
    data.ctrl.roomCtrls.forEach(function (elem) {
      var limits = elem.limits;
      var prices = elem.prices;
      var tabView = $(cat).find('[data-tabviewid=' + elem.id + ']')[0];
      changeSelect(tabView, '[name="guest"]', limits.guestsCnt);
      changeSelect(tabView, '[name="addGuest"]', limits.addGuestsCnt);
      (!limits.twin) ? 
      $(tabView).find('[data-twin]').attr({
        'disabled': true,
        'data-twin': false
      })
      : $(tabView).find('[data-twin]').removeAttr('disabled').attr('data-twin', true);
      for(var key in prices) {
        console.log(key);
        //TODO: remove spaces from key in more general place!!
        $(tabView).find('[data-tariff-name=' + key.replace(/\s/g, '') + ']').find('[name="tariff-price"]').val(prices[key]);
      }
    });
    tariffSum(cat);
    
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
    console.log('FROM', from, 'TO', to);
    return $.ajax(jsRoutes.controllers.Application.category(from, to, req, JSON.stringify(createFiltersReqObj(collectFilters($('.filtering')[0])))));
  };
    
  var usualResponceHandling = function(ask, cat, e){

    globalFilt.specialResponce = false;
    
    ask.done(function(resp) {
      console.log('usualResponceHandling', resp);
      
      // var addDays = function () {
      //   $(e.target).parent()
      //   .find('.additional-days, .time-no-available').hide();
      
      //   (resp.ctrl.eci && e.target.name === 'timeIn') ? $(e.target).parent().find('.eci').show() : $(e.target).parent().find('.eci').hide();
        
      //   (resp.ctrl.lco && e.target.name === 'timeOut') ? $(e.target).parent().find('.lco').show() : $(e.target).parent().find('.lco').hide();
        
      //   $(cat).find('input').not('[name="timeIn"], [name="timeOut"], [data-twin]').
      //   removeAttr('disabled');
      //   if ($(cat).find('[data-twin]').is('[data-twin=false]')) {
      //     $(cat).find('[data-twin=false]').attr('disabled', true);
      //   }
      // };

      if ($(cat).find('[data-twin]').is('[data-twin=false]')) {
        $(cat).find('[data-twin=false]').attr('disabled', true);
      }
      
      if (resp.type === 'basic') {
        // console.log('basic');
        setupOpt(resp, cat);
        // addDays();
        return;
      }
      
      if (resp.type === 'tariffsRedraw') {
        // console.log('tariffsRedraw');
        $(cat).find('.tariffs').empty().append(resp.html);
        setupOpt(resp, cat);
        // addDays();
        return;
      }
      
      if (resp.type === 'fullRedraw') {
        // console.log('fullRedraw');
        $(cat).replaceWith(resp.html);
        var newCat = $('[data-catId=' + cat.dataset.catid +']');
        saveInitCatCtrl(newCat);
        categoryGallery(newCat);
        beautySelect(newCat);
        categoryTimepicker();
        return;
      }
      
      if (resp.type === 'gone') {
        // console.log('gone');
        $(cat).remove();
        delete catCtrl[cat.dataset.catid];
        return;
      }
      
    })
      
  };
    
  var specialResponceHandling = function(ask, cat, e, date) {

    globalFilt.specialResponce = globalFilt.specialResponce || true;

    globalFilt.specialFROM = date.from;
    globalFilt.specialTO = date.to;

    var $target = $(e.target);
        // hotelCI = stringToMinutes($(cat).parent().data('eci')),
        // hotelCO = stringToMinutes($(cat).parent().data('lco')),
        // catCI   = stringToMinutes($(cat).find('[name="timeIn"]').val()),
        // catCO   = stringToMinutes($(cat).find('[name="timeOut"]').val()),
        // early   = $(cat).find('.eci'),
        // late    = $(cat).find('.lco');
    
    ask.done(function(resp) {
      console.log('specialResponceHandling', resp, 'TARGET PARENT', $target.parent()[0]);
      // $target.parent().find('.eci, .lco').hide(); 
        
      if (resp.type === 'basic' || resp.type === 'tariffsRedraw') {
        console.log('resp.type === basic || resp.type === tariffsRedraw');
        // $target.parent().find('.cico, .time-no-available').hide();

        if ( date.from == globalFilt.from ) {
          console.log('HIDE EARLY DAY', $(cat).find('.option-early .additional-days')[0]);
          $(cat).find('.option-early .additional-days').hide();
          (resp.ctrl.eci) ? early.show() : early.hide();
        } else {
          console.log('SHOW EARLY DAY', $(cat).find('.option-early .additional-days')[0]);
          $(cat).find('.option-early .additional-days').show();
        }

        if ( date.to == globalFilt.to ) {
          console.log('HIDE LATE DAY', $(cat).find('.option-late .additional-days')[0]);
          $(cat).find('.option-late .additional-days').hide();
          (resp.ctrl.lco) ? late.show() : late.hide();
        } else {
          console.log('SHOW LATE DAY', $(cat).find('.option-late .additional-days')[0]);
          $(cat).find('.option-late .additional-days').show();
        }

        setupOpt(resp, cat);
        
        return;
      }
    
      if (resp.type === 'fullRedraw' || resp.type === 'gone') {
        console.log('resp.type === fullRedraw || resp.type === gone');
        target.parent().find('.cico, .additional-days').hide();
        target.parent().find('.time-no-available').show();
        
        $(cat).find('input').not('[name="timeIn"], [name="timeOut"]').
        attr('disabled', 'true');
        
        return;
      }
      
    })
    
  };
  
  var tabs = function (cat, amount, tabsList, catSettingsList) {
    
    var catSettings     = catCtrl[cat.dataset.catid].clone();
        tabsList        = cat.querySelector(tabsList),
        catSettingsList = cat.querySelector(catSettingsList),
        amount          = amount,
        tabsLength      = tabsList.children.length;
        
    var populatetabs = function () {
      
      tabsList.style.display = 'block'; 
      
      if (amount > tabsLength) {
        var cnt = amount - tabsLength;
        
        for(var i = 1; i <= cnt; i++) {
          var id = tabsLength + i;
          
          var tab = $(tabsList)
          .children()
          .first()
          .clone()
          .attr('data-tabid', id)
          .removeAttr('data-tabInit')
          .removeClass('tabs-item--active');
          tab.find('span').text('Комната №' + id);
          
          $(tabsList).append(tab);
          
          var settings = catSettings.clone();
          settings.attr('data-tabviewid', id);
          settings.hide();
          catSettingsList.appendChild(settings[0]);
          
        }
        
      } else {
        var cnt = tabsLength - amount;
        
        for(i = 0; i < cnt; i++) {
          $(tabsList).children().last().remove();
          $(catSettingsList).children().last().remove();
        }
      }
      
      if (!$(tabsList).children().is('.tabs-item--active')) {
        $(tabsList).children().first().addClass('tabs-item--active');
      }
      
      $(cat).find('.cat-settings-item').hide();
      $(cat).find('[data-tabviewid=' + $(cat).find('.tabs-item--active').data('tabid') + ']').show();
      beautySelect(cat);
      
      $(cat).find('.tabs-item').click(function (e) {
        $(cat).find('.tabs-item').removeClass('tabs-item--active');
        $(e.currentTarget).addClass('tabs-item--active');
        $(cat).find('.cat-settings-item').hide();
        $(cat).find('[data-tabviewid=' + e.currentTarget.dataset.tabid + ']').show();
      });
      
    };
    
    var cleartabs = function () {
      $(tabsList).children().each(function (index, elem) {
        if (index !== 0) $(elem).remove();  
      });
      tabsList.style.display = 'none';
      $(catSettingsList).children().each(function (index, elem) {
        (index !== 0) ? $(elem).remove() : $(elem).show();
      });
      beautySelect(cat);
    };
    
    (amount > 1) ? populatetabs() : cleartabs();

    console.log('TABS: askfor()', askFor);

    if (globalFilt.specialResponce) {
      var from = globalFilt.specialFROM;
      var to = globalFilt.specialTO;
    } else {
      var from = moment($('#checkIn').val(), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss');
      var to = moment($('#checkOut').val(), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss');
    }

    askFor(
      from, 
      to, 
      stringOpts(cat))
      .done(function (resp) {
        console.log(resp);
        setupOpt(resp, cat);
        changeSelect(cat, '[name=room_count]', resp.ctrl.maxRoomCnt);
      });
    
  };

  var changeCat = function (cat) {

    cat = cat || '.category-list';

    $(cat).change(function(e) {
      // console.log('CHANGECAT', e.target);
      
      var cat  = $('.category').has(e.target)[0];
      
      if (e.target.name === 'room_count') {
        tabs(cat, e.target.value, '.tabs', '.cat-settings');
        tariffSum(cat);
        return;
      }

      if (e.target.name === 'tariff-btn') {
        switchTariff(cat, e.target);
        return;
      }

      var req       = stringOpts(cat),
          from      = moment($('#checkIn').val(), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss'),
          to        = moment($('#checkOut').val(), 'YYYYMMDDHHmmss').format('YYYYMMDDHHmmss'),
          formatStr = 'YYYYMMDDHHmmss',
          hotelCI   = stringToMinutes($(cat).parent().data('eci')),
          hotelCO   = stringToMinutes($(cat).parent().data('lco')),
          catCI     = stringToMinutes($('#checkIn').val()),
          catCO     = stringToMinutes($('[name="timeOut"]').val());
          
          console.log('REQUEST', req);
          console.log('FROM', from);
          console.log('TO', to);

      // if (catCI <= hotelCI || catCO >= hotelCO) {
      //   var tempFROM = moment(globalFilt.from.toDate());
      //   from = +from.format(formatStr);

      //   if (catCI <= hotelCI) {
      //     var now = moment().tz('Europe/Kiev');
      //     var f = 'YYYYMMDD';
      //     if (tempFROM.subtract(1, 'd').format(f) === now.format(f)) {
      //       from = +now.add(1, 'h').format(formatStr);
      //     } else {
      //       from = +tempFROM.format(formatStr);
      //     }
      //   } 

      //   console.log('CHANGECAT FROM', from);

      //   if (catCO >= hotelCO) {
      //     var tempTO = moment(globalFilt.to.toDate());
      //     to = +tempTO.add(1, 'd').format(formatStr)
      //   } else {
      //     to = +to.format(formatStr);
      //   }

        // specialResponceHandling(
        //   askFor(from, to, req), 
        //   cat, 
        //   e, 
        //   {from: from, to: to});

      // } else {

      //   from = +from.format(formatStr);
      //   to   = +to.format(formatStr);

      // }

      usualResponceHandling(askFor(from, to, req), cat, e);

    });

  };

  var changeFilter = function (selector) {
    console.time('changeFilter');
    $(selector).change(function(e) {
      window.from = collectFilters(selector).from;
      // console.log(collectFilters(selector));
        var container = e.currentTarget,
              req  = createFiltersReqObj(collectFilters(selector)),
              // from = +collectFilters(selector).from.format('YYYYMMDDHHmmss'),
              from = setupFrom(collectFilters(selector).from),
              to   = +collectFilters(selector).to.format('YYYYMMDDHHmmss');
              console.log(from);

        var ci = stringToMinutes(moment($('#checkIn').val(), 'YYYYMMDDHHmmss').format('HH:mm'));
        var co = stringToMinutes(moment($('#checkOut').val(), 'YYYYMMDDHHmmss').format('HH:mm'));
        var eci = stringToMinutes("9:00");
        var lco = stringToMinutes("20:00");
        var hotelCheckIn = stringToMinutes("13:00");
        var hotelCheckOut = stringToMinutes("12:00");
        console.log(`ci ${ci}, co ${co}, eci ${eci}, lco ${lco}`);        

        if (ci < eci) {
          $('#checkIn').parent().find('.additional-days, .option-data-eci').show();
          $('#checkIn').parent().find('.eci, .time-no-available').hide();
          var checkInDate = $('#checkIn').val();
          from = moment(checkInDate, 'YYYYMMDD13:00').subtract(1, 'd').format('YYYYMMDDHHmmss');
          console.log(`CI < ECI, checkInDate: ${checkInDate}`);
        }

        if (co > lco) {
          $('#checkOut').parent().find('.additional-days, .option-data-lco').show();
          $('.lco, .time-no-available').hide();
          var checkOutDate = $('#checkOut').val();
          to = moment(checkOutDate, 'YYYYMMDD12:00').add(1, 'd').format('YYYYMMDDHHmmss');
          console.log(`CO > LCO, checkOutDate: ${checkOutDate}`);
        }

        if (ci >= eci && ci <= hotelCheckIn) {
          $('#checkIn').parent().find('.additional-days, .time-no-available').hide();
          $('#checkIn').parent().find('.eci, .option-data-eci').show();
          console.log(`ci >= eci && ci <= hotelCheckIn`);
        } else if (ci > hotelCheckIn) {
          $('#checkIn').parent().find('.eci, .additional-days, .time-no-available').hide();
        }

        if (co >= hotelCheckOut && co <= lco) {
          $('#checkOut').parent().find('.additional-days, .time-no-available').hide();
          $('#checkOut').parent().find('.lco, .option-data-lco').show();
          console.log(`co <= lco && co >= hotelCheckOut`);
        } else if (co < hotelCheckOut) {
          $('#checkOut').parent().find('.lco, .time-no-available, .additional-days').hide();
        }

        // var addDays = function () {
        //   $(e.target).parent()
        //   .find('.additional-days, .time-no-available').hide();
        
        //   (resp.ctrl.eci && e.target.name === 'timeIn') ? $(e.target).parent().find('.eci').show() : $(e.target).parent().find('.eci').hide();
          
        //   (resp.ctrl.lco && e.target.name === 'timeOut') ? $(e.target).parent().find('.lco').show() : $(e.target).parent().find('.lco').hide();
          
        //   $(cat).find('input').not('[name="timeIn"], [name="timeOut"], [data-twin]').
        //   removeAttr('disabled');
        // };
              
          $.ajax(jsRoutes.controllers.Application.filter(from, to, JSON.stringify(req)))
          .done(function( response ) {

            $('.category-list').replaceWith(response);
            
            saveInitCatCtrl();

            categoryGallery('.category-list');

            categoryTimepicker(from);

            beautySelect('.category-list');

            changeCat();

            tariffSum('.category-list .category');

            console.timeEnd('changeFilter');
          });
    });
  };
  
  var catCtrl = {};  
    
  var saveInitCatCtrl = function (category) {
    var selector = category || '.category';
    $(selector).each(function (index, elem) {
      var catSettings = $(elem).find('.cat-settings-item').clone();
      catCtrl[elem.dataset.catid] = catSettings;
    })
  };

//   ************************Starting dynamic****************************

  $.ajax(jsRoutes.controllers.Application.getDummyOffers(from, to))
    .done(function( resp ) {
      $('.filtering').after(resp);
      console.log(from, to);
      window.from = from;
      window.to = to;
      
      saveInitCatCtrl();
      
      $('#checkIn').val(moment(from, 'YYYYMMDDHHmmss').format('YYYY.MM.DD  HH:mm'));
      $('#checkOut').val(moment(to, 'YYYYMMDDHHmmss').format('YYYY.MM.DD  HH:mm'));

      beautySelect('.category-list, .filtering');

      changeCat('.category-list');

      changeFilter($('.filtering')[0]);

      tariffSum('.category-list .category');

      categoryGallery('.category-list');

      categoryTimepicker(from);
      
      (function () {
        
        var min = moment($('#checkIn').val(), 'YYYYMMDDHHmmss');
        var max = moment($('#checkOut').val(), 'YYYYMMDDHHmmss');

        // console.log(min.format('YYYY.MM.DD'), max.format('YYYY.MM.DD'));

        $('#checkOut').datetimepicker({
            format:         'YYYY.MM.DD  HH:mm',
            formatDate:     'YYYY.MM.DD  HH:mm',
            formatTime:     'HH.mm',
            step:           30,
            timepicker:     true,
            validateOnBlur: false,
            scrollMonth:    false,
            scrollTime:     false,
            minDate:        min.add(1, 'd').format('YYYY.MM.DD')
        });

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
            maxDate:        max.subtract(1, 'd').format('YYYY.MM.DD')
        });

        
      })();

      $('#checkIn, #checkOut').trigger('change');

  });

window.globalFilt = globalFilt;  

});
