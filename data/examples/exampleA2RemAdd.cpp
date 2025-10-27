int x = 0;
#ifdef Feature1
    x *= x;
#endif
#ifdef Feature3
	x += 300;
	#ifdef Feature2
	 	x *= 3;
	#endif
    x += 12;
#endif
#ifdef FeatureA
 z *= 100;
#endif
