#include <pebble.h>
#define KEY_DATA 5

static Window *my_window;
static TextLayer *time_layer, *address_text_layer, *distance_text_layer;
//BitmapLayer *direction_arrow;
static Layer *path_layer;

static const GPathInfo ARROW_PATH_POINTS = {
  7,
  (GPoint []) {
    {-20, -5},
    {0, -25},
    {20, -5},
    {10, -5},
    {10, 25},
    {-10, 25},
    {-10, -5}
  }
};

static GPath *arrow_path;
static GPath *current_path = NULL;
static int path_angle = 0;

static void path_layer_update_callback(Layer *me, GContext *ctx) {
  (void)me;
  // You can rotate the path before rendering
  gpath_rotate_to(current_path, (TRIG_MAX_ANGLE / 360) * path_angle);
  graphics_context_set_fill_color(ctx, GColorBlack);
  gpath_draw_filled(ctx, current_path);
}

static int path_angle_add(int angle) {
  return path_angle = (path_angle + angle) % 360;
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Rotate the path counter-clockwise
  path_angle_add(-10);
  layer_mark_dirty(path_layer);
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Rotate the path clockwise
  path_angle_add(10);
  layer_mark_dirty(path_layer);
}

static void config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

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
  
  text_layer_set_text(time_layer, buffer);
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

  // Time
  time_layer = text_layer_create(GRect(0, 0, window_bounds.size.w, window_bounds.size.h/2));
  text_layer_set_font(time_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_text_color(time_layer, GColorBlack);
  text_layer_set_text_alignment(time_layer, GTextAlignmentCenter);
  text_layer_set_text(time_layer, "00:00");
  layer_add_child(window_layer, text_layer_get_layer(time_layer));
  
  // Address
  
  address_text_layer = text_layer_create(GRect(0, 104, window_bounds.size.w, window_bounds.size.h));
  text_layer_set_font(address_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_14));
  text_layer_set_text_color(address_text_layer, GColorBlack);
  text_layer_set_overflow_mode(address_text_layer, GTextOverflowModeFill);
  text_layer_set_text_alignment(address_text_layer, GTextAlignmentCenter);
  text_layer_set_text(address_text_layer, "123 University Ave.");
  layer_add_child(window_layer, text_layer_get_layer(address_text_layer));
  
  // Distance
                                 
  distance_text_layer = text_layer_create(GRect(0, 120, window_bounds.size.w, window_bounds.size.h));
  text_layer_set_font(distance_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_color(distance_text_layer, GColorBlack);
  text_layer_set_overflow_mode(distance_text_layer, GTextOverflowModeWordWrap);
  text_layer_set_text_alignment(distance_text_layer, GTextAlignmentCenter);
  text_layer_set_text(distance_text_layer, "43 metres");
  layer_add_child(window_layer, text_layer_get_layer(distance_text_layer));
  
  // Direction arrow                               
  /*
  direction_arrow = bitmap_layer_create(GRect(5, 0, window_bounds.size.w - 5, window_bounds.size.h));
  bitmap_layer_set_alignment(direction_arrow, GAlignTop);
  layer_add_child(window_layer, bitmap_layer_get_layer(direction_arrow));
  */
  
  // Path
  
  path_layer = layer_create(window_bounds);
  layer_set_update_proc(path_layer, path_layer_update_callback);
  layer_add_child(window_layer, path_layer);
  arrow_path = gpath_create(&ARROW_PATH_POINTS);
  current_path = arrow_path;
  gpath_move_to(current_path, GPoint(window_bounds.size.w/2, window_bounds.size.h/2 - 10));
                                 
  update_time();
}

static void main_window_unload(Window *window) {
  text_layer_destroy(time_layer);
  text_layer_destroy(address_text_layer);
  text_layer_destroy(distance_text_layer);
// bitmap_layer_destroy(direction_arrow);
  gpath_destroy(arrow_path);
  layer_destroy(path_layer);
}

static void tick_handler(struct tm *tick_time, TimeUnits units_changed) {
  update_time();
}

void handle_init(void) {

  // Create main Window
  my_window = window_create();

  time_layer = text_layer_create(GRect(0, 0, 144, 20));
  
  window_set_window_handlers(my_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
//  window_set_click_config_provider(my_window, click_config_provider);
  
  window_stack_push(my_window, true);
  window_set_click_config_provider(my_window, config_provider);
  
  tick_timer_service_subscribe(MINUTE_UNIT, tick_handler);
  
}

void handle_deinit(void) {
  window_destroy(my_window);
}

int main(void) {
  handle_init();
  app_event_loop();
  handle_deinit();
}
