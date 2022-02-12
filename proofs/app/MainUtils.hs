module MainUtils where

headlineMarker :: String
headlineMarker = ">>>>>>>"

headline :: String -> IO()
headline s = putStrLn $ headlineMarker ++ " " ++ s ++ " " ++ headlineMarker

linebreak :: IO ()
linebreak = putStrLn ""
