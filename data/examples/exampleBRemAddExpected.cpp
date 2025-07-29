int x = 0;
#ifdef Feature1
    x *= x;
	#ifdef FeatureB
    	int b = 987;
	#endif
#endif
#ifdef Feature3
	x += 300;
	#ifdef Feature2
	 	x *= 3;
	#endif
    x += 12;
#endif
