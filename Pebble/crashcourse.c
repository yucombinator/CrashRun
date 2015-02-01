#include <pebble.h>
#include <stdio.h>
#include <stdlib.h>

static Window *main_window;
static TextLayer *timer_layer, *cardinal_text_layer, *distance_text_layer;
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
  gpath_rotate_to(current_path, (TRIG_MAX_ANGLE / 360) * path_angle);
  graphics_context_set_fill_color(ctx, GColorBlack);
  gpath_draw_filled(ctx, current_path);
}

static int path_angle_add(int angle) {
  return path_angle = (path_angle + angle) % 360;
}

// Buttons

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

// Rotation

void rotate_arrow(const int angle){
  
  if (angle > 157.5 && angle <= 202.5){
    path_angle = 180;
    text_layer_set_text(cardinal_text_layer, "South");
  } else if (angle > 202.5 && angle <= 247.5){
    path_angle = 225;
    text_layer_set_text(cardinal_text_layer, "Southwest");
  } else if (angle > 247.5 && angle <= 292.5){
    path_angle = 270;
    text_layer_set_text(cardinal_text_layer, "West");
  } else if (angle > 292.5 && angle <= 337.5){
    path_angle = 315;
    text_layer_set_text(cardinal_text_layer, "Northwest");
  } else if ((angle > 337.5 && angle <= 360) || (angle >= 0 && angle <= 22.5)){
    path_angle = 0;
    text_layer_set_text(cardinal_text_layer, "North");
  } else if (angle > 22.5 && angle <= 67.5){
    path_angle = 45;
    text_layer_set_text(cardinal_text_layer, "Northeast");
  } else if (angle > 67.5 && angle <= 112.5){
    path_angle = 90;
    text_layer_set_text(cardinal_text_layer, "East");
  } else if (angle > 112.5 && angle <= 157.5){
    path_angle = 135;
    text_layer_set_text(cardinal_text_layer, "Southeast");
  } 
}

// App communication

static void inbox_received_callback(DictionaryIterator *iterator, void *context) {

  Tuple *t = dict_read_first(iterator);

  while (t != NULL) {
    
    static char distance[64], timer[64];
    
    switch (t->key) {
      case 0:
        snprintf(distance, sizeof(distance), "%s", t->value->cstring);
        text_layer_set_text(distance_text_layer, distance);
        break;
      case 1:
        //snprintf(angle, sizeof(angle), "%s", (int)t->value->int32;
        //int result = atoi(angle);
        rotate_arrow((int)t->value->int32);
        break;
      case 5:
        snprintf(timer, sizeof(timer), "%s", t->value->cstring);
        text_layer_set_text(timer_layer, timer);
        break;
      case 6:
        if((t->value->int8)>0){
          vibes_long_pulse();
        }
        break;
    }
    
    t = dict_read_next(iterator);
    
  }
}

// Window

static void main_window_load(Window *window) {
  
  Layer *window_layer = window_get_root_layer(window);
  GRect window_bounds = layer_get_bounds(window_layer);

  // Timer
  timer_layer = text_layer_create(GRect(0, 0, window_bounds.size.w, window_bounds.size.h/2));
  text_layer_set_font(timer_layer, fonts_get_system_font(FONT_KEY_GOTHIC_24));
  text_layer_set_text_color(timer_layer, GColorBlack);
  text_layer_set_text_alignment(timer_layer, GTextAlignmentCenter);
  text_layer_set_text(timer_layer, "00:00");
  layer_add_child(window_layer, text_layer_get_layer(timer_layer));
  
  // Cardinal directions
  
  cardinal_text_layer = text_layer_create(GRect(0, 100, window_bounds.size.w, window_bounds.size.h));
  text_layer_set_font(cardinal_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_color(cardinal_text_layer, GColorBlack);
  text_layer_set_overflow_mode(cardinal_text_layer, GTextOverflowModeFill);
  text_layer_set_text_alignment(cardinal_text_layer, GTextAlignmentCenter);
  text_layer_set_text(cardinal_text_layer, "North");
  layer_add_child(window_layer, text_layer_get_layer(cardinal_text_layer));
  
  // Distance
  
  distance_text_layer = text_layer_create(GRect(0, 120, window_bounds.size.w, window_bounds.size.h));
  text_layer_set_font(distance_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_color(distance_text_layer, GColorBlack);
  text_layer_set_overflow_mode(distance_text_layer, GTextOverflowModeWordWrap);
  text_layer_set_text_alignment(distance_text_layer, GTextAlignmentCenter);
  text_layer_set_text(distance_text_layer, "0 metres");
  layer_add_child(window_layer, text_layer_get_layer(distance_text_layer));
  
  // Path
  
  path_layer = layer_create(window_bounds);
  layer_set_update_proc(path_layer, path_layer_update_callback);
  layer_add_child(window_layer, path_layer);
  arrow_path = gpath_create(&ARROW_PATH_POINTS);
  current_path = arrow_path;
  gpath_move_to(current_path, GPoint(window_bounds.size.w/2, window_bounds.size.h/2 - 10));
            
}

static void main_window_unload(Window *window) {
  text_layer_destroy(timer_layer);
  text_layer_destroy(cardinal_text_layer);
  text_layer_destroy(distance_text_layer);
  gpath_destroy(arrow_path);
  layer_destroy(path_layer);
}

void handle_init(void) {
  
  app_message_register_inbox_received(inbox_received_callback);
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
  
  // Create Windows
  main_window = window_create();
  
  window_set_window_handlers(main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
  
  window_stack_push(main_window, true);
  window_set_click_config_provider(main_window, config_provider);
  
}

void handle_deinit(void) {
  window_destroy(main_window);
}

int main(void) {

  handle_init();
  app_event_loop();
  handle_deinit();
}
