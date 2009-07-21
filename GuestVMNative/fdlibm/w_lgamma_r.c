
 /* @(#)w_lgamma_r.c	1.9 05/11/17           */
/*
 * @(#)w_lgamma_r.c	1.9 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * wrapper double lgamma_r(double x, int *signgamp)
 */

#include "fdlibm.h"


#ifdef __STDC__
	double lgamma_r(double x, int *signgamp) /* wrapper lgamma_r */
#else
	double lgamma_r(x,signgamp)              /* wrapper lgamma_r */
        double x; int *signgamp;
#endif
{
#ifdef _IEEE_LIBM
	return __ieee754_lgamma_r(x,signgamp);
#else
        double y;
        y = __ieee754_lgamma_r(x,signgamp);
        if(_LIB_VERSION == _IEEE_) return y;
        if(!finite(y)&&finite(x)) {
            if(floor(x)==x&&x<=0.0)
                return __kernel_standard(x,x,15); /* lgamma pole */
            else
                return __kernel_standard(x,x,14); /* lgamma overflow */
        } else
            return y;
#endif
}
