int x = 0;
#ifdef Feature1
	x += 100;
	#ifdef FeatureB
		int b = 1234;
	#endif
	#ifdef Feature2
	 	int y = 50;
	#endif
    x *= x;
#endif
#ifdef Feature3
	x += 300;
	#ifdef FeatureB
		b = 987;
	#endif
#endif
