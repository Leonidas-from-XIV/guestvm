
 /* @(#)w_cosh.c	1.9 05/11/17           */
/*
 * @(#)w_cosh.c	1.9 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * wrapper cosh(x)
 */

#include "fdlibm.h"

#ifdef __STDC__
	double cosh(double x)		/* wrapper cosh */
#else
	double cosh(x)			/* wrapper cosh */
	double x;
#endif
{
#ifdef _IEEE_LIBM
	return __ieee754_cosh(x);
#else
	double z;
	z = __ieee754_cosh(x);
	if(_LIB_VERSION == _IEEE_ || isnan(x)) return z;
	if(fabs(x)>7.10475860073943863426e+02) {
	        return __kernel_standard(x,x,5); /* cosh overflow */
	} else
	    return z;
#endif
}
