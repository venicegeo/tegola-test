// perfrunner
package main

import (
	"errors"
	"flag"
	"fmt"
	"os"
	"strings"
	"time"

	tegola "github.com/terranodo/tegola"
	vegeta "github.com/tsenart/vegeta/lib"
)

func main() {
	// Define command-line parameters
	baseURL := flag.String("baseURL", "http://www.example.com", "the start of the url to attack")
	zoom := flag.Int("zoom", 1, "the zoom level")
	rate := flag.Int("rate", 1, "requests per second")
	timeout := flag.Int("timeout", 5, "time before request is cancelled, minutes")
	duration := flag.Int("duration", 60, "seconds to run test")
	bounds := flag.String("bounds", "-10,-10,10,10", "the bounding box of the location to be load tested. 'min_lon, min_lat, max_lon, max_lat'")
	city := flag.String("city", "", "You can supply a city name in lieu of specifying bounds.  Replaces spaces with underscores.")
	flag.Parse()

	// Parse the provided "bounds" variable to get the bounding box in integers.
	var minLon float64
	var minLat float64
	var maxLon float64
	var maxLat float64
	var boundsStr string
	switch strings.ToUpper(*city) {
	case "":
		boundsStr = *bounds
	case "DENVER":
		boundsStr = "-105.11,39.61,-104.62,39.91"
	case "LOS_ANGELES":
		boundsStr = "-118.95,32.8,-117.65,34.82"
	case "NEW_YORK":
		boundsStr = "-74.26,40.48,-73.7,40.92"
	case "TOKYO":
		boundsStr = "-74.26,40.48,-73.7,40.92"
	default:
		check(errors.New("The city " + *city + " does not have bounds."))
	}
	_, err := fmt.Sscanf(boundsStr, "%f,%f,%f,%f", &minLon, &minLat, &maxLon, &maxLat)
	check(err)

	// Print the settings used.
	fmt.Printf("Attacking base URL, %v\n", *baseURL)
	fmt.Printf("At %v times per second\n", *rate)
	fmt.Printf("For %v seconds\n", *duration)

	// Create the results file.
	t := time.Now()
	filename := fmt.Sprintf("%v_%v_Z%v_R%v.csv", t.Format("2006-01-02_15.04.05"), *city, *zoom, *rate)
	f, err := os.Create(filename)
	check(err)

	// Get the row/column bounds, given the coordinate bounds.
	minx, miny, maxx, maxy := getTileRange(*zoom, minLon, minLat, maxLon, maxLat)

	// Initialize the array of targets vegeta will attack.
	totalTiles := (maxx - minx + 1) * (maxy - miny + 1)
	targets := make([]vegeta.Target, totalTiles)

	// Print the x,y,z values used.
	fmt.Printf("Requesting tiles at zoom level %v\n", *zoom)
	fmt.Printf("In rows from %v to %v\n", miny, maxy)
	fmt.Printf("In columns from %v to %v\n", minx, maxx)
	fmt.Printf("A total of %v different tiles will be tested, if time allows.\n", totalTiles)

	// Iterate through the possible row/column pairs, filling in the array of targets.
	index := 0
	for i := minx; i <= maxx; i++ {
		for j := miny; j <= maxy; j++ {
			// Build the URL,
			url := fmt.Sprintf("%v/%v/%v/%v.pbf",
				*baseURL,
				*zoom,
				i,
				j)
			// then use it to create the Target.
			targets[index] = vegeta.Target{
				Method: "GET",
				URL:    url,
			}
			// Then move to the next index of the array of targets.
			index++
		}
	}

	// Run vegeta attack
	timeDuration := time.Duration(*duration) * time.Second
	timeoutDuration := time.Duration(*timeout) * time.Minute
	targeter := vegeta.NewStaticTargeter(targets...)
	attacker := vegeta.NewAttacker(vegeta.Timeout(timeoutDuration))
	var metrics vegeta.Metrics
	results := attacker.Attack(targeter, uint64(*rate), timeDuration)
	for res := range results {
		metrics.Add(res)
		vegeta.NewCSVEncoder(f).Encode(res)
	}
	metrics.Close()

	// Examine Results, and determine pass/fail
	for code, count := range metrics.StatusCodes {
		fmt.Printf("Total %3v received: %3v\n", code, count)
	}
	fmt.Printf("Total Duration: %v\n", metrics.Duration.Seconds())
	fmt.Printf("Success Rate: %v\n", metrics.Success)
	fmt.Printf("Errors: %v\n", metrics.Errors)
	fmt.Printf("Average Response Time: %v\n", metrics.Latencies.Mean)
	fmt.Printf("Longest Response Time: %v\n", metrics.Latencies.Max)
}

// returns minx, miny, maxx, maxy
func getTileRange(zoom int, minlon float64, minlat float64, maxlon float64, maxlat float64) (int, int, int, int) {
	// Get the row/coumn of the top-left tile.
	topLeft := tegola.Tile{Z: zoom, Long: minlon, Lat: maxlat}
	minx, miny := topLeft.Deg2Num()

	// Get the row/column of the bottom-right tile.
	bottomRight := tegola.Tile{Z: zoom, Long: maxlon, Lat: minlat}
	maxx, maxy := bottomRight.Deg2Num()
	return minx, miny, maxx, maxy
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}
