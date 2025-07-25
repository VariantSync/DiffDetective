int x = 0;
#ifdef Feature1
	x += 100;
	#ifdef Feature2
	 	int y = 50;
	#endif
    x *= x;
	#ifdef FeatureB
    	int b = 987;
	#endif
#endif
#ifdef Feature3
	x += 300;
#endif
