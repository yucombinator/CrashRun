#include <pebble.h>
#define KEY_DATA 5

Window *my_window;
TextLayer *text_layer, *address_text_layer, *distance_text_layer;
BitmapLayer *direction_arrow;
double lat_coord, long_coord;

//Time

static void update_time() {
  
  time_t temp = time(NULL);
  struct tm *tick_time = localtime(&temp);
  
  static char buffer[] = "00:00";
  
  if(clock_is_24h_style() == true){
    strftime(buffer, sizeof("00:00"), "%H:%M", tick_time);
  } else {
    strftime(buffer, sizeof("00:00"), "%I:%M", tick_time);
  }
  
  text_layer_set_text(text_layer, buffer);
}

/* Button clicks

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up pressed!");
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Select pressed!");
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down pressed!");
}


static void click_config_provider(void *context) {
  // Register the ClickHandlers
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
 // window_single_click_subscribe(BUTTON_ID_BACK, back_click_handler);

}
*/

// Windows

static void main_window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect window_bounds = layer_get_bounds(window_layer);

  // Create output layers
  text_layer = text_layer_create(GRect(5, 0, window_bounds.size.w - 5, window_bounds.size.h));
  text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_overflow_mode(text_layer, GTextOverflowModeWordWrap);
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  
  address_text_layer = text_layer_create(GRect(0, 0, window_bounds.size.w - 5, window_bounds.size.h));
  text_layer_set_font(address_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_overflow_mode(address_text_layer, GTextOverflowModeWordWrap);
  text_layer_set_text_alignment(address_text_layer, GTextAlignmentCenter);
  
  distance_text_layer = text_layer_create(GRect(5, 0, window_bounds.size.w - 5, window_bounds.size.h));
  text_layer_set_font(distance_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_overflow_mode(distance_text_layer, GTextOverflowModeWordWrap);
  text_layer_set_text_alignment(distance_text_layer, GTextAlignmentCenter);
  
  direction_arrow = bitmap_layer_create(GRect(5, 0, window_bounds.size.w - 5, window_bounds.size.h));
  bitmap_layer_set_alignment(direction_arrow, GAlignTop);
  
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
  layer_add_child(window_layer, text_layer_get_layer(address_text_layer));
  layer_add_child(window_layer, text_layer_get_layer(distance_text_layer));
  layer_add_child(window_layer, bitmap_layer_get_layer(direction_arrow));
  
  update_time();
}

static void main_window_unload(Window *window) {
  // Destroy layers
  text_layer_destroy(text_layer);
  text_layer_destroy(address_text_layer);
  text_layer_destroy(distance_text_layer);
  bitmap_layer_destroy(direction_arrow);
}

static void tick_handler(struct tm *tick_time, TimeUnits units_changed) {
  update_time();
}

void handle_init(void) {

  // Create main Window
  my_window = window_create();

  text_layer = text_layer_create(GRect(0, 0, 144, 20));
  
  window_set_window_handlers(my_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
//  window_set_click_config_provider(my_window, click_config_provider);
  
  window_stack_push(my_window, true);
  
  tick_timer_service_subscribe(MINUTE_UNIT, tick_handler);
  
}

void handle_deinit(void) {
  text_layer_destroy(text_layer);
  window_destroy(my_window);
}

int main(void) {
  handle_init();
  app_event_loop();
  handle_deinit();
}
