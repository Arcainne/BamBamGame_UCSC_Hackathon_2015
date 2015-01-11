#define DISKMAIN
  
#ifdef DISKMAIN
#include "pebble.h"

#define ACCEL_STEP_MS 50
#define NUM_ACCEL_SAMPLES 1

#define HEIGHT 156
#define WIDTH 144

  
static Window *window;

static GRect window_frame;

static Layer *bargraph_layer;

static AppTimer *timer;

static int count;

static int accel_x[WIDTH];
static int accel_y[WIDTH];
static int accel_z[WIDTH];
TextLayer *phase_layer, *score_layer, *role_layer, *gesture_layer;
enum {
    KEY_GESTURE = 0,
    GESTURE_1 = 1,
    GESTURE_2 = 2,
    GESTURE_3 = 3,
    KEY_SEND_ROLE= 4,
    KEY_SEND_PHASE=5,
    KEY_SCORE_UPDATE= 6,
    WAITING_ROOM_SCREEN= 7,
    GAME_PLAY_SCREEN=8,
    FINAL_SCREEN=9  
};

///--------------------FUNCTIONS--------------------------------

//GRAPHICAL DISPLAY:

void draw_bar_graph(GContext* ctx, GPoint origin, GSize size, int* list) {
  int i;
  //draw background rect (clear out old stuff)
  GRect backgndRect = { .origin = origin, .size = size };
  graphics_context_set_fill_color(ctx,	GColorWhite);	
  graphics_fill_rect(	ctx, backgndRect, 0, GCornerNone );
  //draw bar graph
  for(i=0 ; i < (int)WIDTH ; i++) {
    int xorg=origin.x+i;
    int xdest=xorg; //vertial lines
    int yorg=origin.y + size.h/2;
    int ydest=yorg + list[i]/100;
    //make sure it fits:
    if(ydest < origin.y) ydest = origin.y;
    if(ydest > origin.y + size.h) ydest = origin.y + size.h;
    
    graphics_draw_line(	ctx,
      (GPoint) {xorg, yorg},
      (GPoint) {xdest, ydest} 
    );
  }
}

static TextLayer* init_text_layer(GRect location, GTextAlignment alignment, char * aFont)
{
  TextLayer *layer = text_layer_create(location);
  text_layer_set_text_color(layer, GColorBlack);
  text_layer_set_background_color(layer, GColorClear);
  text_layer_set_font(layer, fonts_get_system_font(aFont));
  text_layer_set_text_alignment(layer, alignment);
  return layer;
}

       
       
static void bargraph_layer_update_callback(Layer *me, GContext *context) {
  draw_bar_graph(context, (GPoint){0, 0*HEIGHT/3},(GSize){WIDTH,HEIGHT/3-1}, accel_x);
  draw_bar_graph(context, (GPoint){0, 1*HEIGHT/3},(GSize){WIDTH,HEIGHT/3-1}, accel_y);
  draw_bar_graph(context, (GPoint){0, 2*HEIGHT/3},(GSize){WIDTH,HEIGHT/3-1}, accel_z);
}

//ACCELEROMETRY:
static void accel_handler(AccelData *data, uint32_t num_samples){
  AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };
  accel_service_peek(&accel);
  int i;
  //first shift the lists to the right (Ew gross!)
  for(i=(WIDTH-1); i>0; i--){
    accel_x[i]=accel_x[i-1];
    accel_y[i]=accel_y[i-1];
    accel_z[i]=accel_z[i-1];
  }
  //and then drop in the new value
  accel_x[0]=data[0].x;
  accel_y[0]=data[0].y;
  accel_z[0]=data[0].z;
  if (accel_x[0] >= abs(750)) {
  text_layer_set_text(gesture_layer, "Last Gesture: Gesture 1");
    count = 20;
  }else if(accel_y[0] >= abs(750)){
  text_layer_set_text(gesture_layer, "Last Gesture: Gesture 2");
    count = 20;
  }else if(accel_z[0] >= abs(750)){
  text_layer_set_text(gesture_layer, "Last Gesture: Gesture 3");
    count = 20;
  }else if(count<=0){
    text_layer_set_text(gesture_layer, "Last Gesture: None");
  }count--;
  
  
}


static void timer_callback(void *data) {
  
  layer_mark_dirty(bargraph_layer);

  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect frame = window_frame = layer_get_frame(window_layer);

  bargraph_layer = layer_create(frame);
  layer_set_update_proc(bargraph_layer, bargraph_layer_update_callback);
  layer_add_child(window_layer, bargraph_layer);
  
  
  gesture_layer = init_text_layer(GRect(5, 90, 144, 30), GTextAlignmentCenter, FONT_KEY_GOTHIC_18);
  text_layer_set_text(gesture_layer, "Last Gesture: N/A");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(gesture_layer));
}

static void window_unload(Window *window) {
  //layer_destroy(bargraph_layer);
  text_layer_destroy(gesture_layer);
}


// static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
//   text_layer_set_text(text_layer, "Up");
// }

// static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
//   text_layer_set_text(text_layer, "Down");
// }

// static void click_config_provider(void *context) {
//   window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
//   window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
//   window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
// }


static void init(void) {
  window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload
  });
  window_stack_push(window, true /* Animated */);
  window_set_background_color(window, GColorBlack);

  accel_data_service_subscribe(0, NULL);

  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
  
  //subscribe to accel data service:
  accel_data_service_subscribe(NUM_ACCEL_SAMPLES, accel_handler);
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}

static void deinit(void) {
  accel_data_service_unsubscribe();

  window_destroy(window);
}


int main(void) {
  init();
  app_event_loop();
  deinit();
}
#endif