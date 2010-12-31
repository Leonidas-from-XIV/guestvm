#ifndef _MAXWELL_SIMPLEMON_H_
#define _MAXWELL_SIMPLEMON_H_

#include <sched.h>

/* A monitor can be in one of three states:
   1. unowned, indicated by holder == NULL
   2. owned by holder, no waiters
   3. owned by holder, list of waiters.
   In state 2 or 3, the recursion count may be > 0
*/
typedef struct maxve_monitor {
    struct thread *holder;
    long rcount;
    struct list_head waiters;
    spinlock_t lock;
    long contend_count;
    long uncontend_count;
} maxve_monitor_t;

maxve_monitor_t *maxve_monitor_create(void);
int maxve_monitor_enter(maxve_monitor_t *monitor);
int maxve_monitor_exit(maxve_monitor_t *monitor);

typedef struct maxve_condition {
    spinlock_t lock;
    struct list_head waiters;
} maxve_condition_t;

maxve_condition_t *maxve_condition_create(void);
int maxve_condition_wait(maxve_condition_t *condition, maxve_monitor_t *monitor, struct timespec *timespec);
int maxve_condition_notify(maxve_condition_t *condition, int all);

#endif
