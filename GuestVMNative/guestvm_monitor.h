#ifndef _MAXWELL_SIMPLEMON_H_
#define _MAXWELL_SIMPLEMON_H_

#include <sched.h>

/* A monitor can be in one of three states:
   1. unowned, indicated by holder == NULL
   2. owned by holder, no waiters
   3. owned by holder, list of waiters.
   In state 2 or 3, the recursion count may be > 0
*/
typedef struct guestvmXen_monitor {
    struct thread *holder;
    long rcount;
    struct list_head waiters;
    spinlock_t lock;
    long contend_count;
    long uncontend_count;
} guestvmXen_monitor_t;

guestvmXen_monitor_t *guestvmXen_monitor_create(void);
int guestvmXen_monitor_enter(guestvmXen_monitor_t *monitor);
int guestvmXen_monitor_exit(guestvmXen_monitor_t *monitor);

typedef struct guestvmXen_condition {
    spinlock_t lock;
    struct list_head waiters;
} guestvmXen_condition_t;

guestvmXen_condition_t *guestvmXen_condition_create(void);
int guestvmXen_condition_wait(guestvmXen_condition_t *condition, guestvmXen_monitor_t *monitor, struct timespec *timespec);
int guestvmXen_condition_notify(guestvmXen_condition_t *condition, int all);

#endif
