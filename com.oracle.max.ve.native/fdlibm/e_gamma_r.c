/*
 * Copyright (c) 2006, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/* __ieee754_gamma_r(x, signgamp)
 * Reentrant version of the logarithm of the Gamma function
 * with user provide pointer for the sign of Gamma(x).
 *
 * Method: See __ieee754_lgamma_r
 */

#include "fdlibm.h"

#ifdef __STDC__
	double __ieee754_gamma_r(double x, int *signgamp)
#else
	double __ieee754_gamma_r(x,signgamp)
	double x; int *signgamp;
#endif
{
	return __ieee754_lgamma_r(x,signgamp);
}
