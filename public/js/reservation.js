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
    
    var roomOpts = $(cat).find('.cat-settings-item').map(function (index, elem) {
     return {
       id: +elem.dataset.tabviewid,
       guestsCnt: elemAndVal(elem, '[data-guestscnt]', 'number').value,
       addGuestsCnt: elemAndVal(elem, '[data-addguests]', 'number').value,
       twin: elemAndVal(elem, '[data-twin]').value,
       bkf: elemAndVal(elem, '[data-breakfast]').value
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
    return filters;
  };

  var stringOpts = function (cat) {
    console.time('stringOpts');

    var opt = collectOpts(cat);
    console.log(opt.roomReqs);

    var data = JSON.stringify({
      'hotelId':          opt.hotelId,
      'catId':            opt.catId,
      'roomReqs':         opt.roomReqs,
      'tariffGroupsHash': opt.hash,
      'ci':               opt.eci.value,
      'co':               opt.lco.value
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
      (limits.twin) ? 
      $(tabView).find('.option-twin').css('display', 'inline-block')
      : $(tabView).find('.option-twin').css('display', 'none')
      for(var key in prices) {
        $(tabView).find('[data-tariff-name=' + key + ']').find('.tariff-price').text(prices[key] + ' грн');
      }
    });
    
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
    
    ask.done(function(resp) {
      console.log('usualResponceHandling', resp);
      
      var addDays = function () {
        $(e.target).parent().find('.additional-days, .time-no-available').hide();
        $(cat).find('.item-not-available').hide();
      
        (resp.ctrl.eci && e.target.name === 'timeIn') ? $(e.target).parent().find('.eci').show() : $(e.target).parent().find('.eci').hide();
        
        (resp.ctrl.lco && e.target.name === 'timeOut') ? $(e.target).parent().find('.lco').show() : $(e.target).parent().find('.lco').hide();
        
        $(cat).find('input').not('[name="timeIn"], [name="timeOut"]').
        removeAttr('disabled');
      };
      
      if(resp.type === 'basic') {
        console.log('basic');
        setupOpt(resp, cat);
        addDays();
        return;
      }
      
      if(resp.type === 'tariffsRedraw') {
        console.log('tariffsRedraw');
        $('.tariffs').empty().append(resp.html);
        saveInitCatCtrl();
        setupOpt(resp, cat);
        addDays();
        return;
      }
      
      if(resp.type === 'fullRedraw') {
        console.log('fullRedraw');
        $(cat).replaceWith(resp.html);
        saveInitCatCtrl();
        var newCat = $('[data-catId=' + cat.dataset.catid +']');
        categoryGallery(newCat);
        beautySelect(newCat);
        categoryTimepicker();
        return;
      }
      
      if(resp.type === 'gone') {
        console.log('gone');
        $(cat).remove();
        saveInitCatCtrl();
        return;
      }
      
    })
      
  };
    
  var specialResponceHandling = function(ask, cat, e) {
    
    ask.done(function(resp) {
      console.log('specialResponceHandling', resp);
      $(e.target).parent().find('.eci, .lco').hide();          
        
      if(resp.type === 'basic' || resp.type === 'tariffsRedraw') {
        console.log('resp.type === basic || resp.type === tariffsRedraw');
        $(e.target).parent().find('.cico, .time-no-available').hide();
        $(e.target).parent().find('.additional-days').show();
        
        return;
      }
    
      if(resp.type === 'fullRedraw' || resp.type === 'gone') {
        console.log('resp.type === fullRedraw || resp.type === gone');
        $(e.target).parent().find('.cico, .additional-days').hide();
        $(e.target).parent().find('.time-no-available').show();
        
        $(cat).find('input').not('[name="timeIn"], [name="timeOut"]').
        attr('disabled', 'true');
        $(cat).find('.item-not-available').show();
        
        return;
      }
      
    })
    
  };
  
  var tabs = function (cat, amount, tabsList, catSettingsList) {
    console.log('tabs', amount);
    
    var catSettings     = catCtrl[cat.dataset.catid].clone();
        tabsList        = cat.querySelector(tabsList),
        catSettingsList = cat.querySelector(catSettingsList),
        amount          = amount,
        tabsLength      = tabsList.children.length;
    
    var populatetabs = function () {
      console.log('populatetabs', catSettings);
      
      tabsList.style.display = 'block';
      if(tabsList.dataset.init === 'no') {
        catSettingsList.innerHTML = '';
        tabsList.dataset.init = ''
      }
      
      if(amount > tabsLength) {
        var cnt = amount - tabsLength;
        
        for(i = 1; i <= cnt; i++) {
          var id = tabsLength + i;
          
          var tab = (function () {
            var li = document.createElement('li');
            li.className = 'tabs-item';
            li.setAttribute('data-tabid', id);
            var span = document.createElement('span');
            span.textContent = 'Комната №' + (id);
            li.appendChild(span);
            return li; 
          })();
          
          tabsList.appendChild(tab);
          
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
      
      if(!$(tabsList).children().is('.tabs-item--active')) {
        tabsList.firstChild.classList.add('tabs-item--active');
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
      console.log('cleartabs', catSettings[0]);
      tabsList.innerHTML = '';
      tabsList.style.display = 'none';
      catSettingsList.innerHTML = '';
      catSettingsList.appendChild(catSettings[0]);
      beautySelect(cat);
    };
    
    (amount > 1) ? populatetabs() : cleartabs();
    
  };

  var changeCat = function (cat) {

    cat = cat || '.category-list';

    $(cat).change(function(e) {
      
      var cat  = $('.category').has(e.target)[0];
      
      if(e.target.name === 'room_count') {
        console.log('ROOOOOOOOOOOOOOOOOMS');
        tabs(cat, e.target.value, '.tabs', '.cat-settings');
        return;
      }
      
      var req  = stringOpts(cat),
          from = globalFilt.from;
          to   = globalFilt.to;
          
          console.log(req);
      
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
              
        if(e.target.name === 'timeIn' && catCI <= hotelCI || e.target.name === 'timeOut' && catCO >= hotelCO) {
          
          specialResponceHandling(askFor(from, to, req), cat, e);
          
        } else { 
          usualResponceHandling(askFor(from, to, req), cat, e);
         }
        
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
              
          $.ajax(jsRoutes.controllers.Application.filter(from, to, JSON.stringify(req)))
          .done(function( response ) {

            $('.category-list').replaceWith(response);
            
            saveInitCatCtrl();

            categoryGallery('.category-list');

            categoryTimepicker();

            tariffsOpen();

            beautySelect('.category-list');

            changeCat();
            console.timeEnd('changeFilter');
          });
    });
  };
  
  var catCtrl = {};  
    
  var saveInitCatCtrl = function () {
    $('.category').each(function (index, elem) {
      var catSettings = $(elem).find('.cat-settings-item').clone();
      catCtrl[elem.dataset.catid] = catSettings;
    })
  }

//   ************************Starting dynamic****************************

  $.ajax(jsRoutes.controllers.Application.getDummyOffers(from, to))
    .done(function( resp ) {
      $('.filtering').after(resp);
      
      saveInitCatCtrl();
      
      $('#checkIn').val(moment((from).toString().slice(0,-6)).format('YYYY.MM.DD'));
      $('#checkOut').val(moment((to).toString().slice(0,-6)).format('YYYY.MM.DD'));

      beautySelect('.category-list, .filtering');

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

        categoryGallery('.category-list');

        categoryTimepicker();

        tariffsOpen();

      })();

  });
});
