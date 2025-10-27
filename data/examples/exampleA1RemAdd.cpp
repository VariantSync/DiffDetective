int x = 0;
#ifdef Feature1
	x += 100;
	#ifdef Feature2
	 	int y = 50;
		#ifdef FeatureA
			int z = 100;
		#endif
	#endif
    x *= x;
#endif
#ifdef Feature3
	x += 300;
#endif
#ifdef FeatureA
 z *= 100;
#endif
